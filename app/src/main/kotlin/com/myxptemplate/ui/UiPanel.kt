package com.myxptemplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ════════════════════════════════════════════════════════════════
 *   UI 设置面板
 * ════════════════════════════════════════════════════════════════ */

@Composable
fun UiPanel() {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        SectionTitle("主题")

        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UiSettings.accentPresets.forEach { (_, color) ->
                AccentSwatch(color)
            }
        }

        SectionTitle("面板")

        SliderRow(
            label = "圆角",
            value = UiSettings.menuCornerDp,
            range = 4f..30f,
            onChange = { UiSettings.menuCornerDp = it },
            format = { "${it.toInt()}dp" }
        )
        SliderRow(
            label = "宽度",
            value = UiSettings.menuWidthRatio * 100f,
            range = 50f..95f,
            onChange = { UiSettings.menuWidthRatio = it / 100f },
            format = { "${it.toInt()}%" }
        )
        SliderRow(
            label = "高度",
            value = UiSettings.menuHeightRatio * 100f,
            range = 30f..90f,
            onChange = { UiSettings.menuHeightRatio = it / 100f },
            format = { "${it.toInt()}%" }
        )

        SectionTitle("HUD 功能栏")

        SegmentedRow(
            label = "显示",
            options = HudDisplay.entries.map { it.name },
            selected = UiSettings.hudDisplay.ordinal,
            onSelect = { UiSettings.hudDisplay = HudDisplay.entries[it] }
        )

        SliderRow(
            label = "字号",
            value = UiSettings.hudFontSizeSp,
            range = 9f..22f,
            onChange = { UiSettings.hudFontSizeSp = it },
            format = { "${it.toInt()}sp" }
        )
        SliderRow(
            label = "右距",
            value = UiSettings.hudRightOffsetDp,
            range = 0f..40f,
            onChange = { UiSettings.hudRightOffsetDp = it },
            format = { "${it.toInt()}dp" }
        )
        SliderRow(
            label = "上距",
            value = UiSettings.hudTopOffsetDp,
            range = 0f..60f,
            onChange = { UiSettings.hudTopOffsetDp = it },
            format = { "${it.toInt()}dp" }
        )

        SwitchRow(
            label = "文字阴影",
            checked = UiSettings.hudTextShadow,
            onToggle = { UiSettings.hudTextShadow = it }
        )
        SwitchRow(
            label = "显示水印",
            checked = UiSettings.hudShowWatermark,
            onToggle = { UiSettings.hudShowWatermark = it }
        )

        TextFieldRow(
            label = "水印内容",
            value = UiSettings.hudWatermarkText,
            onChange = { UiSettings.hudWatermarkText = it }
        )

        Spacer(Modifier.height(8.dp))
    }
}

/* ════════════════════════════════════════════════════════════════
 *   小组件
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun SectionTitle(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(UiColors.Accent)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = UiColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f).height(1.dp).background(UiColors.Divider))
    }
}

@Composable
private fun AccentSwatch(color: Color) {
    val selected = UiSettings.accent.value == color.value
    Box(
        Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (selected) Modifier.border(2.dp, Color.White, CircleShape)
                else Modifier.border(1.dp, UiColors.Divider, CircleShape)
            )
            .clickable { UiSettings.accent = color }
    )
}

@Composable
private fun SliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
    format: (Float) -> String
) {
    Row(
        Modifier.fillMaxWidth().height(38.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = UiColors.TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.width(44.dp)
        )
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f).height(24.dp),
            colors = SliderDefaults.colors(
                thumbColor = UiColors.Accent,
                activeTrackColor = UiColors.Accent,
                inactiveTrackColor = UiColors.TrackOff
            )
        )
        Text(
            format(value),
            color = UiColors.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(48.dp)
        )
    }
}

@Composable
private fun SegmentedRow(
    label: String,
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = UiColors.TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.width(44.dp)
        )
        Row(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(UiColors.BgSidebarItem)
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEachIndexed { i, opt ->
                val active = i == selected
                Box(
                    Modifier
                        .weight(1f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) UiColors.Accent else Color.Transparent)
                        .clickable { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        opt,
                        color = if (active) Color.White else UiColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clickable { onToggle(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = UiColors.TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.width(64.dp)
        )
        Box(Modifier.weight(1f))
        MiniSwitch(checked)
    }
}

@Composable
private fun MiniSwitch(checked: Boolean) {
    val trackColor = if (checked) UiColors.Accent else UiColors.TrackOff
    Box(
        Modifier
            .size(width = 36.dp, height = 20.dp)
            .clip(CircleShape)
            .background(trackColor)
            .padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

@Composable
private fun TextFieldRow(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = UiColors.TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.width(64.dp)
        )
        Box(
            Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(UiColors.BgSidebarItem)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (value.isEmpty()) {
                Text(
                    "输入…",
                    color = UiColors.TextSecondary.copy(alpha = 0.4f),
                    fontSize = 12.sp
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = UiColors.TextPrimary,
                    fontSize = 12.sp
                ),
                cursorBrush = SolidColor(UiColors.Accent),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
