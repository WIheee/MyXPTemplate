package com.myxptemplate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.myxptemplate.overlay.OverlayPermission
import com.myxptemplate.overlay.OverlayService
import com.myxptemplate.ui.theme.ComposeEmptyActivityTheme
import com.myxptemplate.util.ModuleAssets
import com.myxptemplate.util.StellarIntegration

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ModuleAssets.init(applicationContext, packageName)
        StellarIntegration.init()

        enableEdgeToEdge()
        setContent {
            ComposeEmptyActivityTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { pad ->
                    HomeScreen(Modifier.padding(pad))
                }
            }
        }

        requestNotificationPermissionIfNeeded()
    }

    override fun onDestroy() {
        StellarIntegration.destroy()
        super.onDestroy()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            .launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/* ════════════════════════════════════════════════════════════════
 *  主页
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun HomeScreen(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val activity = ctx as? ComponentActivity
    var busy by remember { mutableStateOf(false) }

    val overlayGranted = remember {
        mutableStateOf(OverlayPermission.canDraw(ctx))
    }
    val overlayRunning = OverlayService.isRunning
    val stellarReady = StellarIntegration.serviceReady
    val stellarBinder = StellarIntegration.binderConnected

    // 每次重组刷新一下权限状态（用户可能刚从设置页回来）
    overlayGranted.value = OverlayPermission.canDraw(ctx)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0E0E10))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            Icons.Filled.Extension,
            contentDescription = null,
            tint = Color(0xFF9C5F54),
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "MyXPTemplate",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "悬浮窗 · Stellar 特权",
            color = Color(0xFF9C5F54),
            fontSize = 13.sp
        )

        Spacer(Modifier.height(28.dp))

        StatusCard("Stellar 服务",
            when {
                stellarReady -> "已连接并授权"
                stellarBinder -> "已连接，等待授权"
                else -> "未连接（请先启动 Stellar 服务）"
            },
            ok = stellarReady)

        Spacer(Modifier.height(8.dp))
        StatusCard("悬浮窗权限",
            if (overlayGranted.value) "已授予" else "未授予",
            ok = overlayGranted.value)

        Spacer(Modifier.height(8.dp))
        StatusCard("悬浮窗状态",
            if (overlayRunning) "运行中" else "未运行",
            ok = overlayRunning)

        Spacer(Modifier.height(28.dp))

        // ── 主按钮 ──
        if (!overlayGranted.value) {
            Button(
                onClick = {
                    if (busy) return@Button
                    busy = true
                    OverlayPermission.requestViaStellar(ctx) { ok ->
                        busy = false
                        if (ok) {
                            overlayGranted.value = true
                        } else {
                            // 自动失败 → 引导用户手动开
                            activity?.let { OverlayPermission.openSettings(it) }
                        }
                    }
                },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9C5F54),
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (busy) "正在通过 Stellar 授权…"
                    else if (stellarReady) "一键授权悬浮窗（Stellar）"
                    else "前往系统设置授权悬浮窗",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Button(
                onClick = {
                    val intent = Intent(ctx, OverlayService::class.java)
                    if (overlayRunning) {
                        intent.action = OverlayService.ACTION_STOP
                        ctx.startService(intent)
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            ctx.startForegroundService(intent)
                        } else {
                            ctx.startService(intent)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (overlayRunning) Color(0xFF444444) else Color(0xFF9C5F54),
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (overlayRunning) "停止悬浮窗" else "启动悬浮窗",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── 副按钮 ──
        if (!overlayGranted.value) {
            OutlinedButton(
                onClick = { activity?.let { OverlayPermission.openSettings(it) } },
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("手动去系统设置", fontSize = 13.sp, color = Color(0xFFCCCCCC))
            }
        } else if (!stellarReady) {
            OutlinedButton(
                onClick = { /* 等待 Stellar 服务连接，无需操作 */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth().height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "启动 Stellar 管理器后自动连接",
                    fontSize = 13.sp,
                    color = Color(0xFF888888)
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "提示：悬浮窗为全屏窗口但不拦截触摸，空白区域可正常操作下层应用。",
            color = Color(0xFF666666),
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun StatusCard(label: String, value: String, ok: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1A1C))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Color(0xFF888888), fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            if (ok) "●" else "○",
            color = if (ok) Color(0xFF4CAF50) else Color(0xFF666666),
            fontSize = 18.sp
        )
    }
}
