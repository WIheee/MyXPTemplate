package com.myxptemplate.ui

import androidx.compose.runtime.Composable
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore

/**
 * ════════════════════════════════════════════════════════════════
 *   一行式功能行 —— 面板里每个功能只写一次调用
 * ════════════════════════════════════════════════════════════════
 *
 *  以前（20 行）：
 *      MenuActionButton(
 *          title = FeatureStore.label(f),
 *          desc  = FeatureStore.desc(f),
 *          primary = i == 0,
 *          onClick = { if (FeatureStore.run(f) && FeatureStore.toastEnabled) { ... } }
 *      )
 *
 *  现在（1 行）：
 *      ActionRow(f, primary = i == 0)
 *
 *  Toast 反馈、翻译、状态读写全部封装在这两个函数里。
 */

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
        }
    )
}
