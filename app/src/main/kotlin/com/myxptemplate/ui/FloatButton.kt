package com.myxptemplate.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ════════════════════════════════════════════════════════
 *  按钮 —— 两种形态，都跟 MenuSwitchItem 保持同一套
 *  高度 52dp / 圆角 8dp / 水平 14dp 内边距
 * ════════════════════════════════════════════════════════ */

/**
 * @param primary true  = 强调色实心填充（主操作，如"一键通关"）
 *                false = 深色底 + 左侧强调色竖条（次操作，如"重置"）
 */
@Composable
fun MenuActionButton(
    title: String,
    desc: String = "",
    primary: Boolean = false,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val bg by animateColorAsState(
        when {
            primary && pressed -> UiColors.AccentPressed
            primary            -> UiColors.Accent
            pressed            -> UiColors.BgItemPressed
            else               -> UiColors.BgItem
        },
        tween(120), label = "btnBg"
    )
    val scale by animateFloatAsState(
        if (pressed) 0.985f else 1f, tween(120), label = "btnScale"
    )

    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable(interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!primary) {
            Box(
                Modifier
                    .size(width = 3.dp, height = 22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(UiColors.Accent)
            )
            Spacer(Modifier.width(10.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = UiColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (desc.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    desc,
                    color = if (primary) UiColors.TextPrimary.copy(alpha = 0.82f)
                            else UiColors.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/* ════════════════════════════════════════════════════════
 *  开关 —— 从 FloatMenu 里挪出来，供所有面板复用
 * ════════════════════════════════════════════════════════ */

@Composable
fun MenuSwitchItem(
    title: String,
    desc: String,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val trackColor by animateColorAsState(
        if (enabled) UiColors.Accent else UiColors.TrackOff, tween(250)
    )
    val knobOffset by animateDpAsState(if (enabled) 22.dp else 0.dp, tween(250))

    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(UiColors.BgItem)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = UiColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                desc,
                color = UiColors.TextSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(
            Modifier
                .size(width = 48.dp, height = 26.dp)
                .clip(CircleShape)
                .background(trackColor)
                .padding(2.dp)
        ) {
            Box(
                Modifier
                    .offset(x = knobOffset)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(UiColors.TextPrimary)
            )
        }
    }
}

/* ════════════════════════════════════════════════════════
 *  分割线 —— 按钮区与开关区之间的呼吸
 * ════════════════════════════════════════════════════════ */

@Composable
fun SectionDivider(label: String = "") {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(UiColors.Divider))
        if (label.isNotEmpty()) {
            Text(
                label,
                color = UiColors.TextSecondary.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Box(Modifier.weight(1f).height(1.dp).background(UiColors.Divider))
        }
    }
}
