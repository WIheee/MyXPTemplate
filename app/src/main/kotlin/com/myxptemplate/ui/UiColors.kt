package com.myxptemplate.ui

import androidx.compose.ui.graphics.Color

/** 全 UI 的调色板，改这里就能整体换肤。 */
object UiColors {
    val Accent        = Color(0xFF9C5F54)   // 暖棕红强调色
    val AccentPressed = Color(0xFF7E4A41)

    val BgRoot        = Color(0xFF1A1A1A)   // 菜单最外框
    val BgSidebar     = Color(0xFF222222)   // 左侧栏
    val BgSidebarItem = Color(0xFF151515)   // 左侧栏里的块
    val BgContent     = Color(0xFF151517)   // 右侧内容区
    val BgItem        = Color(0xFF1C1C1C)   // 列表项/按钮底
    val BgItemPressed = Color(0xFF2A2A2A)

    val TextPrimary   = Color.White
    val TextSecondary = Color(0xFF888888)
    val Divider       = Color(0xFF252525)
    val TrackOff      = Color(0xFF333333)
}
