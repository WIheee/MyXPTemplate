package com.myxptemplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/* ════════════════════════════════════════════════════════════════
 *  内联数值面板 —— 展开在开关卡片正下方
 *
 *  布局（紧凑，与开关卡片同宽）：
 *      ┌──────────────────────────────────────────┐
 *      │  ×100           [−]  [  滑块  ]  [+]     │
 *      └──────────────────────────────────────────┘
 * ════════════════════════════════════════════════════════════════ */

@Composable
fun InlineNumberPanel(f: Feature) {
    val spec = remember(f) { specOf(f) }
    var value by remember(f) { mutableFloatStateOf(spec.current().toFloat()) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(UiColors.BgSidebarItem)   // 比卡片更深一档，形成层级
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── 当前值 ──
        Text(
            spec.format(value),
            color = UiColors.Accent,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(56.dp)
        )

        Spacer(Modifier.width(6.dp))

        // ── − 步进 ──
        PanelStepButton("−") {
            val v = spec.snap((value - spec.stepBig).coerceAtLeast(spec.min))
            value = v; spec.commit(v)
        }

        // ── 滑块 ──
        Slider(
            value = value,
            onValueChange = { raw ->
                val v = spec.snap(raw)
                value = v; spec.commit(v)
            },
            valueRange = spec.min..spec.max,
            steps = spec.steps,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp),
            colors = SliderDefaults.colors(
                thumbColor         = UiColors.Accent,
                activeTrackColor   = UiColors.Accent,
                inactiveTrackColor = UiColors.TrackOff,
                activeTickColor    = Color.Transparent,
                inactiveTickColor  = Color.Transparent
            )
        )

        // ── + 步进 ──
        PanelStepButton("+") {
            val v = spec.snap((value + spec.stepBig).coerceAtMost(spec.max))
            value = v; spec.commit(v)
        }
    }
}

@Composable
private fun PanelStepButton(text: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
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
 *  数值规格
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
        format   = { "${it.roundToInt()}ms" }
    )

    else -> NumberSpec(
        min = 0f, max = 1f, step = 0f, stepBig = 0f, steps = 0,
        current = { 0 }, apply = { }, format = { "—" }
    )
}
