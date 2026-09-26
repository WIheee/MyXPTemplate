package com.myxptemplate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.myxptemplate.overlay.OverlayState

/**
 * 悬浮层根组件。
 *
 * 布局
 * ────
 *   · HUD 和 Toast 在最外层 —— 无论球模式还是菜单模式都渲染
 *   · 球和菜单用 AnimatedVisibility 切换，有过渡动画
 *   · 渲染窗口始终全屏，触摸穿透由 OverlayService 的触摸代理窗口控制
 *
 * 动画
 * ────
 *   球 → 菜单：球淡出 + 缩到 0.5，菜单淡入 + 从 0.92 放大
 *   菜单 → 球：反向
 */
@Composable
fun FloatRoot(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {

        // ── 常驻：HUD + Toast ─────────────────────────────
        FloatHud()
        FloatToastHost()

        // ── 球模式 ────────────────────────────────────────
        AnimatedVisibility(
            visible = !OverlayState.expanded,
            enter = fadeIn(tween(200)) + scaleIn(tween(240), initialScale = 0.5f),
            exit  = fadeOut(tween(160)) + scaleOut(tween(200), targetScale = 0.5f)
        ) {
            FloatIcon()
        }

        // ── 菜单模式 ──────────────────────────────────────
        AnimatedVisibility(
            visible = OverlayState.expanded,
            enter = fadeIn(tween(220)) + scaleIn(tween(280), initialScale = 0.92f),
            exit  = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.92f)
        ) {
            Box(Modifier.fillMaxSize()) {
                // 透明遮罩：仅用于点击菜单外部关闭菜单
                Box(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures {
                                OverlayState.expanded = false
                            }
                        }
                )
                FloatMenu()
            }
        }
    }
}
