package com.myxptemplate.overlay

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.myxptemplate.ui.FloatRoot
import com.myxptemplate.ui.theme.ComposeEmptyActivityTheme
import com.myxptemplate.util.ModuleAssets
import kotlin.math.abs

/**
 * ════════════════════════════════════════════════════════════════
 *   悬浮窗前台服务
 * ════════════════════════════════════════════════════════════════
 *
 *   双窗口方案 —— 用公开 API 实现「球外触摸穿透」。
 *
 *     ① 渲染窗口（[rootView]）：始终全屏，负责画球 / 菜单 / HUD / Toast，
 *        坐标一律是屏幕绝对坐标。
 *
 *          球模式 → 加 FLAG_NOT_TOUCHABLE，它不参与任何触摸；
 *          菜单模式 → 去掉该 flag，全屏拦截触摸（点外部关菜单）。
 *
 *     ② 触摸代理窗口（[touchView]）：一个透明的空 View，覆盖在球
 *        （外加 padPx 容差）上，独占球的拖拽 / 点击。
 *
 *          球模式 → 可触摸，且带 FLAG_NOT_TOUCH_MODAL，
 *                   所以球以外的触摸穿透到下层 App；
 *          菜单模式 → 加 FLAG_NOT_TOUCHABLE 退回全穿透。
 *
 *   两个窗口的 flags / 位置都由 [applyLayout] 依据
 *   [OverlayState.expanded] 与球的位置统一重算。
 */
class OverlayService : Service() {

    companion object {
        private const val TAG = "MyXP-Overlay"
        private const val CHANNEL_ID = "myxp_overlay"
        private const val NOTIF_ID = 1001

        const val ACTION_STOP = "com.myxptemplate.overlay.STOP"

        /** 渲染窗口与触摸代理窗口共用的基础 flags。 */
        private const val BASE_FLAGS =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

        /** 供 UI 读取的运行状态。 */
        var isRunning: Boolean by mutableStateOf(false)
            private set
    }

    private lateinit var windowManager: WindowManager
    private var rootView: FrameLayout? = null
    private var rootParams: WindowManager.LayoutParams? = null
    private var touchView: View? = null
    private var touchParams: WindowManager.LayoutParams? = null
    private val host = OverlayHost()

    /** 球周围额外的可触摸容差。 */
    private var padPx = 0

    /** 判定「点击」还是「拖拽」的位移阈值。 */
    private var touchSlop = 0

    private var downRawX = 0f
    private var downRawY = 0f
    private var lastRawX = 0f
    private var lastRawY = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "OverlayService onCreate")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        touchSlop = ViewConfiguration.get(this).scaledTouchSlop
        ModuleAssets.init(applicationContext, packageName)
        startForegroundCompat()

        if (!attachOverlay()) {
            stopSelf()
            return
        }
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "OverlayService onDestroy")
        OverlayState.onRelayout = null
        OverlayState.expanded = false
        OverlayState.dragging = false
        detachOverlay()
        isRunning = false
        super.onDestroy()
    }

    // ════════════════════════════════════════════════════════
    //  悬浮窗
    // ════════════════════════════════════════════════════════

    private fun attachOverlay(): Boolean {
        return try {
            val metrics = resources.displayMetrics
            val density = metrics.density
            val ballSizePx = (56 * density).toInt()
            val startX = (24 * density).toInt()
            val startY = (120 * density).toInt()
            padPx = (8 * density).toInt()

            OverlayState.init(
                screenW = metrics.widthPixels,
                screenH = metrics.heightPixels,
                size = ballSizePx,
                startX = startX,
                startY = startY
            )

            val container = FrameLayout(this).apply {
                isClickable = false
                isFocusable = false
                setViewTreeLifecycleOwner(host)
                setViewTreeSavedStateRegistryOwner(host)
                setViewTreeViewModelStoreOwner(host)
            }

            val cv = ComposeView(this).apply {
                isClickable = false
                isFocusable = false
                setViewTreeLifecycleOwner(host)
                setViewTreeSavedStateRegistryOwner(host)
                setViewTreeViewModelStoreOwner(host)
                setContent {
                    ComposeEmptyActivityTheme {
                        FloatRoot()
                    }
                }
            }

            container.addView(
                cv,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            // ── ① 全屏渲染窗口 ─────────────────────────────
            val rp = newOverlayParams(
                width = WindowManager.LayoutParams.MATCH_PARENT,
                height = WindowManager.LayoutParams.MATCH_PARENT,
                flags = BASE_FLAGS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            windowManager.addView(container, rp)
            rootView = container
            rootParams = rp

            // ── ② 触摸代理窗口（透明空 View） ───────────────
            val proxy = View(this).apply {
                isClickable = false
                isFocusable = false
                setOnTouchListener { v, e -> onTouchProxy(v, e) }
            }
            val tp = newOverlayParams(
                width = ballSizePx + padPx * 2,
                height = ballSizePx + padPx * 2,
                flags = BASE_FLAGS
            )
            windowManager.addView(proxy, tp)
            touchView = proxy
            touchParams = tp

            // 状态变化时重算两个窗口的 flags / 位置
            OverlayState.onRelayout = { applyLayout() }
            applyLayout()

            Log.i(TAG, "悬浮窗已添加（全屏渲染窗口 + 触摸代理窗口）")
            true
        } catch (t: Throwable) {
            Log.e(TAG, "添加悬浮窗失败", t)
            false
        }
    }

    private fun newOverlayParams(
        width: Int,
        height: Int,
        flags: Int
    ): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

    /**
     * 依据 [OverlayState] 重算两个窗口的状态。
     *
     *  · 渲染窗口：菜单模式可触摸，球模式不可触摸（触摸全体穿透）。
     *  · 代理窗口：球模式摆到球上并可触摸，菜单模式不可触摸。
     */
    private fun applyLayout() {
        val rp = rootParams ?: return
        val tp = touchParams ?: return
        val expanded = OverlayState.expanded

        val rootFlags =
            if (expanded) BASE_FLAGS
            else BASE_FLAGS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        if (rp.flags != rootFlags) {
            rp.flags = rootFlags
            rootView?.let { windowManager.updateViewLayout(it, rp) }
        }

        val touchFlags =
            if (expanded) BASE_FLAGS or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            else BASE_FLAGS
        tp.flags = touchFlags

        if (!expanded && OverlayState.ballSize > 0) {
            val left = (OverlayState.ballX - padPx).coerceAtLeast(0)
            val top = (OverlayState.ballY - padPx).coerceAtLeast(0)
            tp.x = left
            tp.y = top
            tp.width = (OverlayState.ballX + OverlayState.ballSize + padPx - left).coerceAtLeast(1)
            tp.height = (OverlayState.ballY + OverlayState.ballSize + padPx - top).coerceAtLeast(1)
        }
        touchView?.let { windowManager.updateViewLayout(it, tp) }
    }

    /**
     * 代理窗口的手势处理：在球上拖拽 → 移动球；轻点 → 展开菜单。
     */
    private fun onTouchProxy(v: View, e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downRawX = e.rawX
                downRawY = e.rawY
                lastRawX = e.rawX
                lastRawY = e.rawY
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val x = e.rawX
                val y = e.rawY
                if (!OverlayState.dragging) {
                    if (abs(x - downRawX) + abs(y - downRawY) <= touchSlop) return true
                    OverlayState.dragging = true
                }
                OverlayState.moveBallBy(x - lastRawX, y - lastRawY)
                lastRawX = x
                lastRawY = y
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (!OverlayState.dragging) OverlayState.expanded = true
                OverlayState.dragging = false
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                OverlayState.dragging = false
                return true
            }
        }
        return false
    }

    private fun detachOverlay() {
        touchView?.let {
            try {
                windowManager.removeViewImmediate(it)
            } catch (t: Throwable) {
                Log.w(TAG, "移除触摸代理窗口失败", t)
            }
            touchView = null
            touchParams = null
        }
        rootView?.let {
            try {
                windowManager.removeViewImmediate(it)
            } catch (t: Throwable) {
                Log.w(TAG, "移除悬浮窗失败", t)
            }
            rootView = null
            rootParams = null
        }
    }

    // ════════════════════════════════════════════════════════
    //  前台服务通知
    // ════════════════════════════════════════════════════════

    private fun startForegroundCompat() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "MyXPTemplate 悬浮窗运行状态"
                setShowBadge(false)
            }
            nm.createNotificationChannel(ch)
        }

        val notif: Notification =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(this, CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(this)
            }
                .setContentTitle("MyXPTemplate")
                .setContentText("悬浮窗正在运行")
                .setSmallIcon(android.R.drawable.ic_menu_view)
                .setOngoing(true)
                .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIF_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }
}

/**
 * 给 ComposeView 用的兜底宿主。
 */
private class OverlayHost : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = vmStore

    init {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }
}
