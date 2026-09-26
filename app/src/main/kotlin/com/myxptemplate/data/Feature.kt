package com.myxptemplate.data

/**
 * ════════════════════════════════════════════════════════════════
 *   所有功能的「单一定义源」
 * ════════════════════════════════════════════════════════════════
 *
 *  以前：字符串散落各处，翻译表三份，加个功能要改 5 个地方。
 *  现在：加一个功能 = 加一个枚举项，翻译 / 类型 / 描述全自动。
 *
 *  枚举名 (name) 就是运行时 key，UI 层用类型，存储层用 name。
 */
enum class Feature(
    val labelEn: String,
    val labelZh: String,
    val descEn:  String = "",
    val descZh:  String = "",
    val kind:    Kind   = Kind.Toggle
) {
    // ── Tab ──────────────────────────────────────────────────
    Home     ("Home",     "主页"),
    Combat   ("Combat",   "战斗"),
    Move     ("Move",     "移动"),
    Survival ("Survival", "世界"),
    Render   ("Render",   "视觉"),

    // ── Combat：动作（点一下执行，不保持状态）────────────────
    OneKeyClear("One-Key Clear", "一键通关",
        "Instantly fill counter to 114514", "立即将计数置满 114514",
        Kind.Action),

    Reset      ("Reset Progress", "重置进度",
        "Reset counter back to 0", "将计数重置回 0",
        Kind.Action),

    // ── Combat：开关（会真 Hook 目标）────────────────────────
    Multiplier ("Multiplier", "百倍加速",
        "Each tap adds 100", "每次点击 +100"),

    AutoTap    ("Auto Tap", "自动连点",
        "Auto tap 5 times per second", "每秒自动点击 5 次"),

    // ── Move：视觉占位 ────────────────────────────────────────
    Fly        ("Fly", "飞行",
        "Free fly mode", "允许玩家在空中自由飞行"),
    Speed      ("Speed", "加速",
        "Increase movement speed", "大幅度提升移动速度"),
    NoFall     ("NoFall", "无摔落伤害",
        "Cancel fall damage", "取消高空坠落受到的伤害"),
    Sprint     ("Sprint", "强制疾跑",
        "Always sprint", "始终保持疾跑状态"),
    AirJump    ("AirJump", "空中跳跃",
        "Jump multiple times in air", "允许在半空中进行无限次跳跃"),
    AntiVoid   ("AntiVoid", "虚空回弹",
        "Bounce back when falling into void", "掉入虚空时自动将玩家弹回安全区域"),
    AutoSprint ("AutoSprint", "自动疾跑",
        "Automatically hold sprint", "无需双击前进即可自动保持疾跑"),
    Bhop       ("Bhop", "兔子跳",
        "Bunny hop to increase speed", "通过连续跳跃来获取极快的移动速度"),
    FastStop   ("FastStop", "快速停止",
        "Stop instantly when no keys pressed", "松开按键时瞬间停止移动消除惯性"),
    Flying     ("Flying", "飞行权限",
        "Enable creative flight ability", "赋予玩家类似创造模式的飞行权限"),
    HighJump   ("HighJump", "高跳",
        "Jump higher than normal", "大幅度增加玩家的跳跃高度"),

    // ── Survival：视觉占位 ────────────────────────────────────
    CheatStealer  ("CheatStealer", "自动取物",
        "Instantly steal all items from chests", "打开箱子瞬间拿走所有物品"),
    Scaffold      ("Scaffold", "自动搭路",
        "Automatically place blocks under you", "在脚下自动放置方块搭桥"),
    FastBuilder   ("FastBuilder", "快速搭建",
        "Build structures at extreme speed", "以极快的速度连续放置方块搭建"),
    ClickTeleport ("ClickTeleport", "点击传送",
        "Teleport to the block you click", "瞬间传送到你点击的方块位置"),
    Teleport      ("Teleport", "目标传送",
        "Teleport to targeted entity", "瞬间传送到锁定的目标实体身边"),
    Surround      ("Surround", "环绕目标",
        "Surround target with blocks", "自动在目标周围放置方块将其困住"),
    LockBack      ("LockBack", "锁背",
        "Lock rotation to back of target", "视角强制锁定在目标的背后"),
    Phantom       ("Phantom", "虚影跟随",
        "Phantom entity follow mode", "生成一个虚影跟随模式"),

    // ── Render：本模块 UI ─────────────────────────────────────
    ArrayList ("ArrayList", "功能列表",
        "Show enabled features list", "右上角显示已启用功能"),
    Language  ("Language / Lang", "语言 / Lang",
        "Switch interface to Chinese", "切换界面为中文"),
    Toast     ("Toast Notify", "消息提示",
        "Show popup notifications", "显示消息弹窗通知"),
    ;

    enum class Kind {
        /** 有开 / 关两种状态，会占用 HUD 一行。 */
        Toggle,
        /** 一次性动作，点一下就执行，不保持状态。 */
        Action
    }

    companion object {
        /** 按枚举名反查（用于旧字符串接口兼容）。 */
        fun of(name: String): Feature? = entries.firstOrNull { it.name == name }
    }
}
