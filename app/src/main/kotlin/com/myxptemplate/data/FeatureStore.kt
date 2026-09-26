package com.myxptemplate.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.myxptemplate.util.ModuleAssets

object FeatureStore {

    private const val KEY_TAB = "current_tab"

    private val _toggles = mutableStateMapOf<Feature, Boolean>()
    val activeFeatures = mutableStateListOf<Feature>()

    var multiplierValue  by mutableStateOf(100)
    var autoTapInterval  by mutableStateOf(200L)

    /** 当前展开内联数值面板的功能（null = 无）。同一时间只展开一个。 */
    var expandedNumberFeature by mutableStateOf<Feature?>(null)
        private set

    var currentTab by mutableStateOf(0)
        private set

    val chinese:          Boolean get() = isOn(Feature.Language)
    val toastEnabled:     Boolean get() = isOn(Feature.Toast)
    val arrayListEnabled: Boolean get() = isOn(Feature.ArrayList)

    init {
        _toggles[Feature.ArrayList] = true
        _toggles[Feature.Toast]     = true
        activeFeatures.add(Feature.ArrayList)
        runCatching {
            currentTab = ModuleAssets.prefs()?.getInt(KEY_TAB, 0) ?: 0
        }
    }

    fun isOn(f: Feature): Boolean = _toggles[f] == true

    fun toggle(f: Feature) {
        if (f.kind != Feature.Kind.Toggle) return
        set(f, !isOn(f))
    }

    fun set(f: Feature, on: Boolean) {
        if (f.kind != Feature.Kind.Toggle) return
        _toggles[f] = on
        if (on) { if (f !in activeFeatures) activeFeatures.add(f) }
        else    { activeFeatures.remove(f) }
    }

    fun run(f: Feature): Boolean = when (f) {
        Feature.OneKeyClear -> true
        Feature.Reset       -> true
        else                -> false
    }

    // ── 内联数值面板 ──────────────────────────────────────

    fun hasNumberAdjust(f: Feature): Boolean = when (f) {
        Feature.Multiplier, Feature.AutoTap -> true
        else -> false
    }

    fun toggleNumberPanel(f: Feature) {
        if (!hasNumberAdjust(f)) return
        expandedNumberFeature = if (expandedNumberFeature == f) null else f
    }

    fun closeNumberPanel() { expandedNumberFeature = null }

    fun updateMultiplier(v: Int) {
        multiplierValue = v.coerceIn(1, 10000)
    }

    fun updateAutoTapInterval(ms: Long) {
        autoTapInterval = ms.coerceIn(20L, 2000L)
    }

    fun selectTab(i: Int) {
        if (i == currentTab) return
        currentTab = i
        runCatching {
            ModuleAssets.prefs()?.edit()?.putInt(KEY_TAB, i)?.apply()
        }
    }

    fun label(f: Feature): String = if (chinese) f.labelZh else f.labelEn
    fun desc(f: Feature):  String = if (chinese) f.descZh  else f.descEn
    fun label(name: String): String = Feature.of(name)?.let { label(it) } ?: name
    fun desc(name: String):  String = Feature.of(name)?.let { desc(it) }  ?: ""

    @Deprecated("Use label()", ReplaceWith("label(name)"))
    fun trans(name: String): String = label(name)
}
