// ⚠️ 本文件由 feature_tool.py 从 features.txt 生成，不要手改。
//    修改功能 → 改 features.txt → 跑 python feature_tool.py gen

package com.myxptemplate.data

/** Tab 结构与成员归属；由 features.txt 生成。 */
object Tabs {

    data class TabDef(
        val feature: Feature,
        val icon:    String,
        val actions: List<Feature> = emptyList(),
        val toggles: List<Feature> = emptyList()
    )

    val LIST: List<TabDef> = listOf(
        TabDef(Feature.Home, "texture.png"),
        TabDef(
            Feature.Combat, "user.png",
            actions = listOf(Feature.OneKeyClear, Feature.Reset),
            toggles = listOf(Feature.Multiplier, Feature.AutoTap)
        ),
        TabDef(
            Feature.Move, "modules.png",
            toggles = listOf(
                Feature.Fly, Feature.Speed, Feature.NoFall, Feature.Sprint,
                Feature.AirJump, Feature.AntiVoid, Feature.AutoSprint,
                Feature.Bhop, Feature.FastStop, Feature.Flying, Feature.HighJump
            )
        ),
        TabDef(
            Feature.Survival, "sky.png",
            toggles = listOf(
                Feature.CheatStealer, Feature.Scaffold, Feature.FastBuilder,
                Feature.ClickTeleport, Feature.Teleport, Feature.Surround,
                Feature.LockBack, Feature.Phantom
            )
        ),
        TabDef(
            Feature.Render, "fps.png",
            toggles = listOf(Feature.ArrayList, Feature.Language, Feature.Toast)
        ),
    )
}
