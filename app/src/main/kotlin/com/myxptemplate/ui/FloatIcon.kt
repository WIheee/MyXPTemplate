package com.myxptemplate.ui

import android.graphics.drawable.AnimatedImageDrawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.myxptemplate.data.FeatureStore
import com.myxptemplate.util.ModuleAssets
import kotlin.math.roundToInt

@Composable
fun FloatIcon() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // 横竖屏自适应：横屏下悬浮球稍大一点
    val ballSizeDp = if (isLandscape) 62.dp else 56.dp
    val ballSizePx = with(density) { ballSizeDp.toPx() }

    // 初始位置用 dp 换算，避免不同 dpi 下位置漂移
    val startX = with(density) { 24.dp.toPx() }
    val startY = with(density) { 120.dp.toPx() }

    var position by remember { mutableStateOf(Offset(startX, startY)) }
    var dragging by remember { mutableStateOf(false) }
    val idle = remember { ModuleAssets.loadDrawable("pig.gif") }
    val running = remember { ModuleAssets.loadDrawable("run_pig.gif") }

    val ballModifier = Modifier
        .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
        .size(ballSizeDp)
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { dragging = true },
                onDragEnd = { dragging = false },
                onDragCancel = { dragging = false }
            ) { change, drag ->
                change.consume()
                val newX = (position.x + drag.x).coerceIn(0f, screenWidthPx - ballSizePx)
                val newY = (position.y + drag.y).coerceIn(0f, screenHeightPx - ballSizePx)
                position = Offset(newX, newY)
            }
        }
        .pointerInput(Unit) {
            detectTapGestures { FeatureStore.menuVisible = true }
        }

    if (idle == null) {
        Box(ballModifier, contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .size(42.dp)
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
                val target = if (dragging) (running ?: idle) else idle
                if (iv.drawable !== target) {
                    iv.setImageDrawable(target)
                    (target as? AnimatedImageDrawable)?.start()
                }
            },
            modifier = ballModifier
        )
    }
}
