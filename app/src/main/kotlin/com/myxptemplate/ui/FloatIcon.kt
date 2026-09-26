package com.myxptemplate.ui

import android.graphics.drawable.AnimatedImageDrawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.myxptemplate.overlay.OverlayState
import com.myxptemplate.util.ModuleAssets

/**
 * 悬浮球。
 *
 * 渲染窗口是全屏的，所以位置用 Modifier.offset 以屏幕绝对坐标表达，
 * 不依赖窗口的 x/y。
 *
 * 拖拽 / 点击由 OverlayService 的触摸代理窗口接管（渲染窗口在球模式下
 * 是 FLAG_NOT_TOUCHABLE 的），这里只读 [OverlayState.dragging] 决定
 * 显示哪张动画。
 */
@Composable
fun FloatIcon() {
    val density = LocalDensity.current
    val ballSizeDp = with(density) { OverlayState.ballSize.toDp() }

    val idle = remember { ModuleAssets.loadDrawable("pig.gif") }
    val running = remember { ModuleAssets.loadDrawable("run_pig.gif") }

    val ballModifier = Modifier
        .offset { IntOffset(OverlayState.ballX, OverlayState.ballY) }
        .size(ballSizeDp)

    if (idle == null) {
        Box(ballModifier, contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(ballSizeDp)
                    .clip(CircleShape)
                    .background(Color(0xFF9C5F54)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    } else {
        AndroidView(
            factory = { c ->
                ImageView(c).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setImageDrawable(idle)
                    (idle as? AnimatedImageDrawable)?.start()
                }
            },
            update = { iv ->
                val target = if (OverlayState.dragging) (running ?: idle) else idle
                if (iv.drawable !== target) {
                    iv.setImageDrawable(target)
                    (target as? AnimatedImageDrawable)?.start()
                }
            },
            modifier = ballModifier
        )
    }
}
