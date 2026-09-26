package com.myxptemplate.util

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import roro.stellar.Stellar

/**
 * ════════════════════════════════════════════════════════════════
 *   Stellar 特权服务接入封装
 * ════════════════════════════════════════════════════════════════
 *
 *   serviceReady 是 Compose state，UI 侧直接读即可自动刷新。
 */
object StellarIntegration {

    private const val TAG        = "MyXP-Stellar"
    private const val PERMISSION = "stellar"
    private const val REQ_CODE   = 1001

    /** Stellar 服务是否已连接且已授权。Compose 响应式。 */
    var serviceReady: Boolean by mutableStateOf(false)
        private set

    /** Binder 已连接但还没拿到权限。 */
    var binderConnected: Boolean by mutableStateOf(false)
        private set

    // ── 监听器 ────────────────────────────────────────────

    private val binderReceivedListener = Stellar.OnBinderReceivedListener {
        Log.i(TAG, "Stellar binder 已连接")
        binderConnected = true
        onBinderReady()
    }

    private val binderDeadListener = Stellar.OnBinderDeadListener {
        Log.w(TAG, "Stellar binder 已断开")
        binderConnected = false
        serviceReady = false
    }

    private val permissionResultListener =
        Stellar.OnRequestPermissionResultListener { requestCode, allowed, onetime ->
            Log.i(TAG, "权限请求结果 code=$requestCode allowed=$allowed onetime=$onetime")
            if (requestCode == REQ_CODE && allowed) {
                onBinderReady()
            }
        }

    // ── 生命周期 ──────────────────────────────────────────

    fun init() {
        Stellar.addBinderReceivedListenerSticky(binderReceivedListener)
        Stellar.addBinderDeadListener(binderDeadListener)
        Stellar.addRequestPermissionResultListener(permissionResultListener)
        Log.i(TAG, "StellarIntegration 已启动")
    }

    fun destroy() {
        Stellar.removeBinderReceivedListener(binderReceivedListener)
        Stellar.removeBinderDeadListener(binderDeadListener)
        Stellar.removeRequestPermissionResultListener(permissionResultListener)
        serviceReady = false
        binderConnected = false
        Log.i(TAG, "StellarIntegration 已停止")
    }

    // ── 内部 ──────────────────────────────────────────────

    private fun onBinderReady() {
        if (!Stellar.pingBinder()) {
            Log.w(TAG, "pingBinder 返回 false")
            serviceReady = false
            return
        }
        if (!Stellar.checkSelfPermission()) {
            Log.i(TAG, "未授权，发起请求…")
            Stellar.requestPermission(PERMISSION, REQ_CODE)
            return
        }
        serviceReady = true
        Log.i(TAG, "Stellar 就绪  version=${Stellar.version}  uid=${Stellar.uid}")
    }
}
