package com.myxptemplate.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myxptemplate.data.Feature
import com.myxptemplate.data.FeatureStore

/* ════════════════════════════════════════════════════════════════
 *  Tab 结构定义 —— 类型安全，无字符串
 * ════════════════════════════════════════════════════════════════ */

private data class TabDef(
    val feature: Feature,
    val icon:    String,
    val actions: List<Feature> = emptyList(),   // 上：一次性动作
    val toggles: List<Feature> = emptyList()    // 下：开关
)

private val TABS: List<TabDef> = listOf(
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
    )
)

/* ════════════════════════════════════════════════════════════════
 *  主菜单
 * ════════════════════════════════════════════════════════════════ */

@Composable
fun FloatMenu() {
    val currentTab = FeatureStore.currentTab.coerceIn(0, TABS.lastIndex)
    val cfg = LocalConfiguration.current
    val landscape = cfg.screenWidthDp > cfg.screenHeightDp

    val menuWidth  = if (landscape) (cfg.screenWidthDp  * 0.52f).dp
                     else           (cfg.screenWidthDp  * 0.72f).dp
    val menuHeight = if (landscape) (cfg.screenHeightDp * 0.78f).dp
                     else           (cfg.screenHeightDp * 0.52f).dp

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Row(
            Modifier
                .width(menuWidth)
                .height(menuHeight)
                .clip(RoundedCornerShape(17.dp))
                .background(UiColors.BgRoot)
                .padding(5.dp)
        ) {
            Sidebar(currentTab)
            Spacer(Modifier.width(4.dp))
            ContentPane(TABS[currentTab])
        }
    }
}

/* ════════════════════════════════════════════════════════════════
 *  左侧栏 —— RowScope 扩展，内部可用 weight()
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun RowScope.Sidebar(currentTab: Int) {
    Column(
        Modifier
            .weight(2f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(13.dp))
            .background(UiColors.BgSidebar)
            .padding(4.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(UiColors.BgSidebarItem)
                .clickable { FeatureStore.menuVisible = false },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "LinYun",
                color = UiColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(4.dp))

        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(UiColors.BgSidebarItem)
                .padding(vertical = 3.dp)
        ) {
            TABS.forEachIndexed { i, tab ->
                TabRow(tab, selected = i == currentTab, onClick = { FeatureStore.selectTab(i) })
            }
        }
    }
}

/* ─── TabRow 需要在内层 Column 里均分高度 → ColumnScope 扩展 ─── */

@Composable
private fun ColumnScope.TabRow(tab: TabDef, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(20.dp).alpha(if (selected) 1f else 0.5f)) {
            AssetImage(
                name = tab.icon,
                fallback = tab.feature.name.lowercase(),
                modifier = Modifier.fillMaxSize()
            )
        }
        if (selected) {
            Spacer(Modifier.width(4.dp))
            Text(
                FeatureStore.label(tab.feature),
                color = UiColors.TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ════════════════════════════════════════════════════════════════
 *  右侧内容区 —— RowScope 扩展
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun RowScope.ContentPane(tab: TabDef) {
    Box(
        Modifier
            .weight(8f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(14.dp))
            .background(UiColors.BgContent)
    ) {
        if (tab.feature == Feature.Home) HomePanel()
        else                             FeaturePanel(tab)
    }
}

@Composable
private fun HomePanel() {
    Box(Modifier.fillMaxSize().padding(6.dp)) {
        AssetImage(
            name = "beon.png",
            fallback = "home",
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(13.dp))
        )
    }
}

/* ════════════════════════════════════════════════════════════════
 *  功能面板：按钮在上，分割线居中，开关在下
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun FeaturePanel(tab: TabDef) {
    val chinese = FeatureStore.chinese

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        /* ① 动作按钮区 */
        tab.actions.forEachIndexed { i, f ->
            MenuActionButton(
                title = FeatureStore.label(f),
                desc  = FeatureStore.desc(f),
                primary = i == 0,
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

        /* ② 分隔线 */
        if (tab.actions.isNotEmpty() && tab.toggles.isNotEmpty()) {
            SectionDivider(if (chinese) "开关" else "TOGGLES")
        }

        /* ③ 开关区 */
        tab.toggles.forEach { f ->
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
    }
}
