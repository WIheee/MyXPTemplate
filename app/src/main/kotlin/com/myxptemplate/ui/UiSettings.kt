package com.myxptemplate.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * ════════════════════════════════════════════════════════════════
 *   UI 全局设置
 * ════════════════════════════════════════════════════════════════
 *
 *   所有可调项都是 Compose state —— 改完立即生效，无需重启。
 *   由 UI 设置面板（[UiPanel]）读写。
 */

/** HUD（功能栏）显示模式。 */
enum class HudDisplay { Outline, Bar, Split, None }

object UiSettings {

    // ── 主题 ──────────────────────────────────────────────

    /** 主强调色。 */
    var accent: Color by mutableStateOf(Color(0xFF9C5F54))

    // ── 面板 ──────────────────────────────────────────────

    /** 菜单外框圆角（dp）。 */
    var menuCornerDp: Float by mutableStateOf(17f)

    /** 菜单宽度占屏幕宽度的比例（竖屏）。 */
    var menuWidthRatio: Float by mutableStateOf(0.72f)

    /** 菜单高度占屏幕高度的比例（竖屏）。 */
    var menuHeightRatio: Float by mutableStateOf(0.52f)

    // ── HUD ──────────────────────────────────────────────

    var hudDisplay: HudDisplay by mutableStateOf(HudDisplay.Split)

    /** HUD 字号（sp）。 */
    var hudFontSizeSp: Float by mutableStateOf(13f)

    /** HUD 距右边缘（dp）。 */
    var hudRightOffsetDp: Float by mutableStateOf(8f)

    /** HUD 距上边缘（dp）。 */
    var hudTopOffsetDp: Float by mutableStateOf(8f)

    /** 文字阴影。 */
    var hudTextShadow: Boolean by mutableStateOf(true)

    /** 显示水印。 */
    var hudShowWatermark: Boolean by mutableStateOf(true)

    /** 水印内容。 */
    var hudWatermarkText: String by mutableStateOf("MyXPTemplate")

    /** 水印相对 HUD 字号的倍率。 */
    var hudWatermarkScale: Float by mutableStateOf(1.6f)

    // ── 预设色板 ──────────────────────────────────────────

    /** UI 面板里展示的主题色候选。 */
    val accentPresets: List<Pair<String, Color>> = listOf(
        "暖棕红" to Color(0xFF9C5F54),
        "天蓝"   to Color(0xFF6EC8F1),
        "紫罗兰" to Color(0xFFB49CFF),
        "翠绿"   to Color(0xFF6ECFA8),
        "珊瑚"   to Color(0xFFFF7E6B),
        "雾灰"   to Color(0xFF9AA0A6),
    )
}
