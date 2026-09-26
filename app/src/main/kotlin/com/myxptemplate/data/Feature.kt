// ⚠️ 本文件由 feature_tool.py 从 features.txt 生成，不要手改。
//    修改功能 → 改 features.txt → 跑 python feature_tool.py gen

package com.myxptemplate.data

/** 所有功能的单一定义源；由 features.txt 生成。 */
enum class Feature(
    val labelEn: String,
    val labelZh: String,
    val descEn:  String = "",
    val descZh:  String = "",
    val kind:    Kind   = Kind.Toggle
) {
    // ── Tab 页 ──
    Home("Home", "主页"),
    Combat("Combat", "战斗"),
    Move("Move", "移动"),
    Survival("Survival", "世界"),
    Render("Render", "视觉"),

    // ── Combat ──
    OneKeyClear("One-Key Clear", "一键通关", "Instantly fill counter to 114514", "立即将计数置满 114514", Kind.Action),
    Reset("Reset Progress", "重置进度", "Reset counter back to 0", "将计数重置回 0", Kind.Action),
    Multiplier("Multiplier", "百倍加速", "Each tap adds 100", "每次点击 +100", Kind.Toggle),
    AutoTap("Auto Tap", "自动连点", "Auto tap 5 times per second", "每秒自动点击 5 次", Kind.Toggle),

    // ── Move ──
    Fly("Fly", "飞行", "Free fly mode", "允许玩家在空中自由飞行", Kind.Toggle),
    Speed("Speed", "加速", "Increase movement speed", "大幅度提升移动速度", Kind.Toggle),
    NoFall("NoFall", "无摔落伤害", "Cancel fall damage", "取消高空坠落受到的伤害", Kind.Toggle),
    Sprint("Sprint", "强制疾跑", "Always sprint", "始终保持疾跑状态", Kind.Toggle),
    AirJump("AirJump", "空中跳跃", "Jump multiple times in air", "允许在半空中进行无限次跳跃", Kind.Toggle),
    AntiVoid("AntiVoid", "虚空回弹", "Bounce back when falling into void", "掉入虚空时自动弹回安全区域", Kind.Toggle),
    AutoSprint("AutoSprint", "自动疾跑", "Automatically hold sprint", "无需双击前进自动保持疾跑", Kind.Toggle),
    Bhop("Bhop", "兔子跳", "Bunny hop to increase speed", "连续跳跃来获取极快移速", Kind.Toggle),
    FastStop("FastStop", "快速停止", "Stop instantly when no keys", "松开按键瞬间停止消除惯性", Kind.Toggle),
    Flying("Flying", "飞行权限", "Enable creative flight ability", "赋予类似创造模式的飞行权限", Kind.Toggle),
    HighJump("HighJump", "高跳", "Jump higher than normal", "大幅度增加跳跃高度", Kind.Toggle),

    // ── Survival ──
    CheatStealer("CheatStealer", "自动取物", "Instantly steal all items", "打开箱子瞬间拿走所有物品", Kind.Toggle),
    Scaffold("Scaffold", "自动搭路", "Automatically place blocks", "在脚下自动放置方块搭桥", Kind.Toggle),
    FastBuilder("FastBuilder", "快速搭建", "Build structures at extreme speed", "以极快的速度连续放置方块", Kind.Toggle),
    ClickTeleport("ClickTeleport", "点击传送", "Teleport to the block you click", "瞬间传送到你点击的方块位置", Kind.Toggle),
    Teleport("Teleport", "目标传送", "Teleport to targeted entity", "瞬间传送到锁定的目标实体身边", Kind.Toggle),
    Surround("Surround", "环绕目标", "Surround target with blocks", "自动在目标周围放置方块困住", Kind.Toggle),
    LockBack("LockBack", "锁背", "Lock rotation to back of target", "视角强制锁定在目标的背后", Kind.Toggle),
    Phantom("Phantom", "虚影跟随", "Phantom entity follow mode", "生成一个虚影跟随模式", Kind.Toggle),

    // ── Render ──
    ArrayList("ArrayList", "功能列表", "Show enabled features list", "右上角显示已启用功能", Kind.Toggle),
    Language("Language / Lang", "语言 / Lang", "Switch interface to Chinese", "切换界面为中文", Kind.Toggle),
    Toast("Toast Notify", "消息提示", "Show popup notifications", "显示消息弹窗通知", Kind.Toggle)
    ;

    enum class Kind { Toggle, Action }

    companion object {
        fun of(name: String): Feature? = entries.firstOrNull { it.name == name }
    }
}
