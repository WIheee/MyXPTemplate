package com.myxptemplate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore
import kotlinx.coroutines.delay

/**
 * HUD（功能列表）
 *
 * 动画设计
 * ────────
 *   · 整体：从右上角缩放 + 淡入/淡出
 *   · 条目：逐条从右侧滑入（仅进入时），带 stagger 延迟
 */
@Composable
fun FloatHud() {
    val enabled = FeatureStore.arrayListEnabled && FeatureStore.activeFeatures.isNotEmpty()

    AnimatedVisibility(
        visible = enabled,
        enter = fadeIn(tween(260)) +
                scaleIn(
                    animationSpec = tween(320),
                    initialScale = 0.82f,
                    transformOrigin = TransformOrigin(1f, 0f)
                ),
        exit = fadeOut(tween(180)) +
                scaleOut(
                    animationSpec = tween(220),
                    targetScale = 0.82f,
                    transformOrigin = TransformOrigin(1f, 0f)
                )
    ) {
        val transition = rememberInfiniteTransition(label = "hud")
        val hue by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(2500), RepeatMode.Restart),
            label = "hue"
        )

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                FeatureStore.activeFeatures.forEachIndexed { i, f ->
                    key(f) {
                        HudEntry(f, hue, i)
                    }
                }
            }
        }
    }
}

@Composable
private fun HudEntry(f: Feature, hue: Float, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // stagger 最多前 8 条依次延迟，避免长列表等太久
        delay(minOf(index, 8) * 45L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) +
                slideInHorizontally(tween(300)) { it }
    ) {
        val color = Color.hsv((hue + index * 15f) % 360f, 1f, 1f)
        Row(
            Modifier
                .padding(vertical = 1.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0x80555555)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = FeatureStore.label(f),
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(
                Modifier
                    .width(3.dp)
                    .height(12.dp)
                    .background(color)
            )
        }
    }
}
