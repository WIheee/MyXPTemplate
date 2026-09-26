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
import com.myxptemplate.data.Tabs

/* ════════════════════════════════════════════════════════════════
 *  Tab 结构 —— 从 Tabs.LIST 读（由 features.txt 生成）
 * ════════════════════════════════════════════════════════════════ */

private typealias TabDef = Tabs.TabDef
private val TABS: List<TabDef> = Tabs.LIST

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
 *  左侧栏
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
            Text("LinYun", color = UiColors.TextPrimary,
                 fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
 *  右侧内容区
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
 *  功能面板 —— 每个功能一行
 * ════════════════════════════════════════════════════════════════ */

@Composable
private fun FeaturePanel(tab: TabDef) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        tab.actions.forEachIndexed { i, f ->
            ActionRow(f, primary = i == 0)
        }

        if (tab.actions.isNotEmpty() && tab.toggles.isNotEmpty()) {
            SectionDivider(if (FeatureStore.chinese) "开关" else "TOGGLES")
        }

        tab.toggles.forEach { f ->
            ToggleRow(f)
        }
    }
}
