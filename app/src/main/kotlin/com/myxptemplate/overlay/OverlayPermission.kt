package com.myxptemplate.overlay

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import com.myxptemplate.ModuleConfig
import com.myxptemplate.util.StellarIntegration
import roro.stellar.Stellar

/**
 * ════════════════════════════════════════════════════════════════
 *   SYSTEM_ALERT_WINDOW 授权辅助
 * ════════════════════════════════════════════════════════════════
 *
 *   SYSTEM_ALERT_WINDOW 是「特殊权限」，不受运行时权限 API 管，
 *   Stellar 的 grantRuntimePermission 也管不了。所以走：
 *
 *       1) 先检测是否已授予（Settings.canDrawOverlays）
 *       2) 未授予 → 用 Stellar.newProcess 执行 appops 命令
 *       3) 命令失败 → 引导用户去系统设置页手动开
 */
object OverlayPermission {

    private const val TAG = "MyXP-OverlayPerm"

    /** 是否已经拿到悬浮窗权限。 */
    fun canDraw(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    /**
     * 尝试通过 Stellar 授予 SYSTEM_ALERT_WINDOW。
     * 后台线程执行 appops，完成后主线程回调。
     */
    fun requestViaStellar(context: Context, onDone: (Boolean) -> Unit) {
        if (!StellarIntegration.serviceReady) {
            Log.w(TAG, "Stellar 未就绪，无法自动授权")
            onDone(false)
            return
        }

        val appContext = context.applicationContext
        val pkg = ModuleConfig.MODULE_PACKAGE

        Thread {
            val ok = try {
                val process = Stellar.newProcess(
                    arrayOf(
                        "appops", "set",
                        pkg,
                        "SYSTEM_ALERT_WINDOW",
                        "allow"
                    ),
                    null, null
                )
                val code = process.waitFor()
                process.destroy()
                Log.i(TAG, "appops 退出码=$code")
                code == 0
            } catch (t: Throwable) {
                Log.e(TAG, "appops 执行异常", t)
                false
            }

            val granted = ok && Settings.canDrawOverlays(appContext)
            Handler(Looper.getMainLooper()).post { onDone(granted) }
        }.start()
    }

    /** 跳转到系统「显示在其他应用上层」设置页。 */
    fun openSettings(activity: Activity) {
        try {
            activity.startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${activity.packageName}")
                )
            )
        } catch (t: Throwable) {
            Log.e(TAG, "打开悬浮窗设置页失败", t)
        }
    }
}
