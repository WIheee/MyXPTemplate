package com.myxptemplate

/**
 * ═══════════════════════════════════════════════════════════
 *  模板配置中心 —— 换目标 App 时改这一个文件 + scope.list
 * ═══════════════════════════════════════════════════════════
 *
 *  scope.list 是编译期资源，无法引用 Kotlin 常量，
 *  换包名时务必手动同步：
 *    app/src/main/resources/META-INF/xposed/scope.list
 */
object ModuleConfig {

    // ── 目标 App ──────────────────────────────────────────
    const val TARGET_PACKAGE  = "com.example.x114514"
    const val TARGET_ACTIVITY = "$TARGET_PACKAGE.MainActivity"
    const val MODULE_PACKAGE  = "com.myxptemplate"

    // ── 目标类成员名（HookBridge 反射用）──────────────────
    const val FIELD_COUNT              = "count"
    const val METHOD_ON_TAP            = "onTap"
    const val METHOD_REFRESH           = "refresh"
    const val METHOD_SHOW_CLEAR_DIALOG = "showClearDialog"

    // ── 业务常量 ──────────────────────────────────────────
    const val TARGET_COUNT = 114514

    // ── 悬浮层 ────────────────────────────────────────────
    const val OVERLAY_TAG = "myxp_overlay"
    const val LOG_TAG     = "MyXPTemplate"
}
