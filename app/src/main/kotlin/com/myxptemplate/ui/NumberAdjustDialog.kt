package com.myxptemplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/* ════════════════════════════════════════════════════════════════
 *  数值调节弹窗
 *
 *  触发：点击开关卡片的标题区（Multiplier / AutoTap）
 *  交互：滑块实时生效；− / + 按钮步进；点遮罩或"完成"关闭
 * ════════════════════════════════════════════════════════════════ */

@Composable
fun NumberAdjustDialog() {
    val target = FeatureStore.numberAdjustTarget ?: return

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xB3000000))
            .pointerInput(Unit) {
                detectTapGestures { FeatureStore.closeNumberAdjust() }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .width(280.dp)
                .pointerInput(Unit) {
                    detectTapGestures { /* swallow */ }
                }
                .clip(RoundedCornerShape(16.dp))
                .background(UiColors.BgRoot)
                .padding(18.dp)
        ) {
            NumberAdjustCard(target)
        }
    }
}

@Composable
private fun NumberAdjustCard(target: Feature) {
    val chinese = FeatureStore.chinese
    val spec = remember(target) { specOf(target) }
    var value by remember(target) { mutableFloatStateOf(spec.current().toFloat()) }

    Column(Modifier.fillMaxWidth()) {

        // ── 标题行 ──
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 16.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(UiColors.Accent)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                FeatureStore.label(target),
                color = UiColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (chinese) "数值调节" else "Adjust",
                color = UiColors.TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        // ── 当前值 ──
        Text(
            spec.format(value),
            color = UiColors.Accent,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        // ── 滑块 ──
        Slider(
            value = value,
            onValueChange = { raw ->
                val v = spec.snap(raw)
                value = v
                spec.commit(v)
            },
            valueRange = spec.min..spec.max,
            steps = spec.steps,
            colors = SliderDefaults.colors(
                thumbColor         = UiColors.Accent,
                activeTrackColor   = UiColors.Accent,
                inactiveTrackColor = UiColors.TrackOff,
                activeTickColor    = Color.Transparent,
                inactiveTickColor  = Color.Transparent
            )
        )

        Spacer(Modifier.height(12.dp))

        // ── 步进 ──
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StepButton(if (chinese) "− 减" else "−", Modifier.weight(1f)) {
                val v = spec.snap((value - spec.stepBig).coerceAtLeast(spec.min))
                value = v
                spec.commit(v)
            }
            StepButton(if (chinese) "+ 加" else "+", Modifier.weight(1f)) {
                val v = spec.snap((value + spec.stepBig).coerceAtMost(spec.max))
                value = v
                spec.commit(v)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── 完成 ──
        Box(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(UiColors.Accent)
                .clickable { FeatureStore.closeNumberAdjust() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (chinese) "完成" else "Done",
                color = UiColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StepButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(UiColors.BgItem)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = UiColors.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ════════════════════════════════════════════════════════════════
 *  每个功能的数值规格
 * ════════════════════════════════════════════════════════════════ */

private class NumberSpec(
    val min: Float,
    val max: Float,
    val step: Float,
    val stepBig: Float,
    val steps: Int,
    val current: () -> Number,
    val apply: (Float) -> Unit,
    val format: (Float) -> String
) {
    fun snap(v: Float): Float {
        val clamped = v.coerceIn(min, max)
        return if (step <= 0f) clamped
        else (clamped / step).roundToInt() * step
    }

    fun commit(v: Float) { apply(v) }
}

private fun specOf(f: Feature): NumberSpec = when (f) {

    Feature.Multiplier -> NumberSpec(
        min      = 1f,
        max      = 10000f,
        step     = 1f,
        stepBig  = 100f,
        steps    = 0,
        current  = { FeatureStore.multiplierValue },
        apply    = { FeatureStore.updateMultiplier(it.roundToInt()) },
        format   = { "×${it.roundToInt()}" }
    )

    Feature.AutoTap -> NumberSpec(
        min      = 20f,
        max      = 2000f,
        step     = 10f,
        stepBig  = 50f,
        steps    = 0,
        current  = { FeatureStore.autoTapInterval },
        apply    = { FeatureStore.updateAutoTapInterval(it.roundToLong()) },
        format   = { "${it.roundToInt()} ms" }
    )

    else -> NumberSpec(
        min = 0f, max = 1f, step = 0f, stepBig = 0f, steps = 0,
        current = { 0 }, apply = { }, format = { "—" }
    )
}
