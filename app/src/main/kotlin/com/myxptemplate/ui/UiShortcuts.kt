package com.myxptemplate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore

@Composable
fun ActionRow(f: Feature, primary: Boolean = false) {
    val chinese = FeatureStore.chinese
    MenuActionButton(
        title = FeatureStore.label(f),
        desc  = FeatureStore.desc(f),
        primary = primary,
        onClick = {
            if (FeatureStore.run(f) && FeatureStore.toastEnabled) {
                ToastBus.push(
                    FeatureStore.label(f),
                    if (chinese) "已完成" else "Done",
                    true
                )
            }
        }
    )
}

@Composable
fun ToggleRow(f: Feature) {
    val chinese = FeatureStore.chinese
    val on = FeatureStore.isOn(f)
    val adjustable = FeatureStore.hasNumberAdjust(f)
    val expanded = FeatureStore.expandedNumberFeature == f

    Column {
        MenuSwitchItem(
            title   = FeatureStore.label(f),
            desc    = FeatureStore.desc(f),
            enabled = on,
            onToggle = {
                FeatureStore.toggle(f)
                if (FeatureStore.toastEnabled) {
                    val now = FeatureStore.isOn(f)
                    ToastBus.push(
                        FeatureStore.label(f),
                        when {
                            chinese && now  -> "已开启"
                            chinese && !now -> "已关闭"
                            now             -> "Enabled"
                            else            -> "Disabled"
                        },
                        now
                    )
                }
            },
            onTitleClick = if (adjustable) {
                { FeatureStore.toggleNumberPanel(f) }
            } else null
        )

        // ── 内联数值面板（展开时显示在卡片正下方）──
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)) + expandVertically(tween(220)),
            exit  = fadeOut(tween(140)) + shrinkVertically(tween(180))
        ) {
            InlineNumberPanel(f)
        }
    }
}
