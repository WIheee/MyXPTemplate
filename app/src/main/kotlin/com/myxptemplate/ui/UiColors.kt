package com.myxptemplate.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * 全 UI 的调色板。
 *
 * 强调色从 [UiSettings.accent] 派生，其余固定为暗色主题。
 * 改 UiSettings.accent 就能整体换肤。
 */
object UiColors {

    /** 强调色（可调）。 */
    val Accent: Color get() = UiSettings.accent

    /** 按下态强调色（比 Accent 深 25%）。 */
    val AccentPressed: Color get() = lerp(UiSettings.accent, Color.Black, 0.25f)

    val BgRoot        = Color(0xFF1A1A1A)
    val BgSidebar     = Color(0xFF222222)
    val BgSidebarItem = Color(0xFF151515)
    val BgContent     = Color(0xFF151517)
    val BgItem        = Color(0xFF1C1C1C)
    val BgItemPressed = Color(0xFF2A2A2A)

    val TextPrimary   = Color.White
    val TextSecondary = Color(0xFF888888)
    val Divider       = Color(0xFF252525)
    val TrackOff      = Color(0xFF333333)
}
