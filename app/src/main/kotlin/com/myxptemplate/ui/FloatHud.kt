package com.myxptemplate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore
import kotlinx.coroutines.delay

/* ════════════════════════════════════════════════════════════════
 *   色板 —— 参考 Solstice getThemedColor
 * ════════════════════════════════════════════════════════════════ */

private val HudPalette = listOf(
    Color(0xFFE9A8BC),
    Color(0xFF6EC8F1),
    Color(0xCCFFFFFF),
)

private fun themedColor(index: Float, progress: Float): Color {
    val total = HudPalette.size
    val pos = (progress * total + index) % total
    val i = pos.toInt()
    val f = pos - i
    return lerp(HudPalette[i % total], HudPalette[(i + 1) % total], f)
}

/* ════════════════════════════════════════════════════════════════
 *   主组件
 * ════════════════════════════════════════════════════════════════ */

@Composable
fun FloatHud() {
    val enabled = FeatureStore.arrayListEnabled && FeatureStore.activeFeatures.isNotEmpty()

    AnimatedVisibility(
        visible = enabled,
        enter = fadeIn(tween(240)) +
                scaleIn(
                    animationSpec = tween(320),
                    initialScale = 0.9f,
                    transformOrigin = TransformOrigin(1f, 0f)
                ),
        exit = fadeOut(tween(160)) +
                scaleOut(
                    animationSpec = tween(200),
                    targetScale = 0.9f,
                    transformOrigin = TransformOrigin(1f, 0f)
                )
    ) {
        val transition = rememberInfiniteTransition(label = "hudColor")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "progress"
        )

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = UiSettings.hudTopOffsetDp.dp,
                        end = UiSettings.hudRightOffsetDp.dp
                    ),
                horizontalAlignment = Alignment.End
            ) {
                if (UiSettings.hudShowWatermark && UiSettings.hudWatermarkText.isNotEmpty()) {
                    WatermarkText(progress)
                    Spacer(Modifier.height(6.dp))
                }

                val sorted = FeatureStore.activeFeatures.sortedByDescending {
                    FeatureStore.label(it).length
                }

                sorted.forEachIndexed { i, f ->
                    key(f) { HudEntry(f, i, progress) }
                }
            }
        }
    }
}

/* ════════════════════════════════════════════════════════════════
 *   Watermark —— 逐字符彩色
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun WatermarkText(progress: Float) {
    val text = UiSettings.hudWatermarkText
    val size = (UiSettings.hudFontSizeSp * UiSettings.hudWatermarkScale).sp

    Row(verticalAlignment = Alignment.CenterVertically) {
        text.forEachIndexed { i, c ->
            val color = themedColor(i * 0.15f, progress)
            Text(
                text = c.toString(),
                style = TextStyle(
                    color = color,
                    fontSize = size,
                    fontWeight = FontWeight.Bold,
                    shadow = hudShadow()
                )
            )
        }
    }
}

/* ════════════════════════════════════════════════════════════════
 *   单条
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun HudEntry(f: Feature, index: Int, progress: Float) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(minOf(index, 8) * 40L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)) + slideInHorizontally(tween(300)) { it }
    ) {
        val color = themedColor(index * 0.1f, progress)
        val label = FeatureStore.label(f)
        val barHeight = (UiSettings.hudFontSizeSp + 2f).dp
        val splitHeight = (UiSettings.hudFontSizeSp * 0.8f).dp

        Row(
            Modifier.padding(vertical = 1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (UiSettings.hudDisplay) {

                HudDisplay.Bar -> {
                    HudText(label, color)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .width(2.dp)
                            .height(barHeight)
                            .background(color)
                    )
                }

                HudDisplay.Split -> {
                    HudText(label, color)
                    Spacer(Modifier.width(6.dp))
                    Box(
                        Modifier
                            .width(3.dp)
                            .height(splitHeight)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(color)
                    )
                }

                HudDisplay.Outline -> {
                    Box(
                        Modifier
                            .border(1.dp, color, RoundedCornerShape(2.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        HudText(label, color)
                    }
                }

                HudDisplay.None -> {
                    HudText(label, color)
                }
            }
        }
    }
}

@Composable
private fun HudText(label: String, color: Color) {
    Text(
        text = label,
        style = TextStyle(
            color = color,
            fontSize = UiSettings.hudFontSizeSp.sp,
            fontWeight = FontWeight.Medium,
            shadow = hudShadow()
        )
    )
}

private fun hudShadow(): Shadow =
    if (UiSettings.hudTextShadow) {
        Shadow(
            color = Color(0x80000000),
            offset = Offset(1f, 1f),
            blurRadius = 1.5f
        )
    } else {
        Shadow(
            color = Color.Transparent,
            offset = Offset.Zero,
            blurRadius = 0f
        )
    }
