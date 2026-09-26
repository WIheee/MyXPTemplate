package com.myxptemplate.xposed

import android.os.Handler
import android.os.Looper
import com.myxptemplate.ModuleConfig
import com.myxptemplate.util.L
import com.myxptemplate.util.NativeBridge
import com.myxptemplate.util.Reflect.callVoidMethod
import com.myxptemplate.util.Reflect.getIntField
import com.myxptemplate.util.Reflect.setIntField
import java.lang.ref.WeakReference

/**
 * ════════════════════════════════════════════════════════════════
 *   目标应用桥接
 * ════════════════════════════════════════════════════════════════
 *
 *  策略：
 *    attach 时探测 —— NativeBridge 可用则缓存 jfieldID / jmethodID，
 *    之后所有操作走 JNI 直调；否则整条链路回退 Java 反射。
 *
 *  性能差异（参考值）：
 *    Java 反射 applyMultiplier  ≈ 1.2 μs
 *    Native applyMultiplier     ≈ 0.08 μs
 */
object HookBridge {

    const val TARGET_COUNT = ModuleConfig.TARGET_COUNT

    private val handler = Handler(Looper.getMainLooper())
    private var activityRef = WeakReference<Any>(null)

    @Volatile var multiplier: Int = 1
    private var autoTapRunnable: Runnable? = null

    /** 本次 attach 是否用上了 native。 */
    @Volatile private var nativeReady = false

    // ═════════════════════════════════════════════════════════
    //  生命周期
    // ═════════════════════════════════════════════════════════

    fun attach(act: Any) {
        activityRef = WeakReference(act)

        // 探测 + 缓存
        nativeReady = NativeBridge.available &&
                      L.attempt("cacheTarget") { NativeBridge.cacheTarget(act) } == true

        L.i { "HookBridge attached, native=$nativeReady" }
    }

    fun detach() {
        stopAutoTap()
        if (nativeReady) L.attempt("native release") { NativeBridge.release() }
        nativeReady = false
        activityRef.clear()
    }

    fun isReady(): Boolean = activityRef.get() != null
    fun isCurrent(act: Any): Boolean = activityRef.get() === act

    private fun getActivity(): Any? = activityRef.get()

    // ═════════════════════════════════════════════════════════
    //  读 / 写 count
    // ═════════════════════════════════════════════════════════

    fun getCount(): Int {
        val act = getActivity() ?: return 0
        return if (nativeReady) NativeBridge.getInt(act)
               else            act.getIntField(ModuleConfig.FIELD_COUNT) ?: 0
    }

    fun setCount(value: Int) {
        val act = getActivity() ?: return
        val v = value.coerceIn(0, TARGET_COUNT)
        runOnMain {
            if (nativeReady) {
                NativeBridge.setInt(act, v)
                NativeBridge.callVoid(act, NativeBridge.Method.REFRESH)
            } else {
                L.attempt("setCount") {
                    act.setIntField(ModuleConfig.FIELD_COUNT, v)
                    act.callVoidMethod(ModuleConfig.METHOD_REFRESH)
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════
    //  高级动作
    // ═════════════════════════════════════════════════════════

    fun oneKeyClear(): Boolean {
        val act = getActivity() ?: return false
        runOnMain {
            if (nativeReady) {
                NativeBridge.setInt(act, TARGET_COUNT)
                NativeBridge.callVoid(act, NativeBridge.Method.REFRESH)
                NativeBridge.callVoid(act, NativeBridge.Method.SHOW_CLEAR_DIALOG)
            } else {
                L.attempt("oneKeyClear") {
                    act.setIntField(ModuleConfig.FIELD_COUNT, TARGET_COUNT)
                    act.callVoidMethod(ModuleConfig.METHOD_REFRESH)
                    act.callVoidMethod(ModuleConfig.METHOD_SHOW_CLEAR_DIALOG)
                }
            }
        }
        return true
    }

    fun resetCount(): Boolean {
        val act = getActivity() ?: return false
        runOnMain {
            if (nativeReady) {
                NativeBridge.setInt(act, 0)
                NativeBridge.callVoid(act, NativeBridge.Method.REFRESH)
            } else {
                L.attempt("resetCount") {
                    act.setIntField(ModuleConfig.FIELD_COUNT, 0)
                    act.callVoidMethod(ModuleConfig.METHOD_REFRESH)
                }
            }
        }
        return true
    }

    // ═════════════════════════════════════════════════════════
    //  点击
    // ═════════════════════════════════════════════════════════

    fun tapOnce() {
        val act = getActivity() ?: return
        runOnMain {
            if (nativeReady) NativeBridge.callVoid(act, NativeBridge.Method.ON_TAP)
            else             act.callVoidMethod(ModuleConfig.METHOD_ON_TAP)
        }
    }

    fun startAutoTap(intervalMs: Long = 200L) {
        stopAutoTap()
        val r = object : Runnable {
            override fun run() {
                tapOnce()
                handler.postDelayed(this, intervalMs)
            }
        }
        autoTapRunnable = r
        handler.post(r)
        L.d { "AutoTap 已启动, interval=${intervalMs}ms" }
    }

    fun stopAutoTap() {
        autoTapRunnable?.let { handler.removeCallbacks(it) }
        autoTapRunnable = null
    }

    // ═════════════════════════════════════════════════════════
    //  Hook 回调：一次点击变 N 倍
    // ═════════════════════════════════════════════════════════

    fun applyMultiplier(act: Any, mult: Int) {
        if (nativeReady) {
            NativeBridge.applyMultiplier(act, mult, TARGET_COUNT)
            return
        }
        L.attempt("applyMultiplier") {
            val cur = act.getIntField(ModuleConfig.FIELD_COUNT) ?: return@attempt
            if (cur >= TARGET_COUNT) return@attempt

            val next = (cur + mult).coerceAtMost(TARGET_COUNT)
            act.setIntField(ModuleConfig.FIELD_COUNT, next)
            act.callVoidMethod(ModuleConfig.METHOD_REFRESH)
            if (next >= TARGET_COUNT) {
                act.callVoidMethod(ModuleConfig.METHOD_SHOW_CLEAR_DIALOG)
            }
        }
    }

    // ═════════════════════════════════════════════════════════
    //  内部
    // ═════════════════════════════════════════════════════════

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block()
        else handler.post(block)
    }
}
