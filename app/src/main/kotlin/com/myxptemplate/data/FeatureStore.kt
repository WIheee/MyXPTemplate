package com.myxptemplate.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.myxptemplate.util.ModuleAssets
import com.myxptemplate.xposed.HookBridge

/**
 * ════════════════════════════════════════════════════════════════
 *   全局状态仓库
 * ════════════════════════════════════════════════════════════════
 *
 *  所有开关状态都在 [_toggles] 里，key 是 [Feature] 枚举而非字符串。
 *  加新功能只要在 Feature 里加一项，这里不用改。
 */
object FeatureStore {

    private const val KEY_TAB = "current_tab"

    // ── 响应式状态 ──────────────────────────────────────────
    private val _toggles = mutableStateMapOf<Feature, Boolean>()

    /** HUD 显示用；顺序即显示顺序。 */
    val activeFeatures = mutableStateListOf<Feature>()

    // ── 数值可调（供以后接 UI 用）────────────────────────────
    var multiplierValue  by mutableStateOf(100)
    var autoTapInterval  by mutableStateOf(200L)

    // ── 菜单可见性 ──────────────────────────────────────────
    var menuVisible by mutableStateOf(false)

    // ── 当前 Tab（持久化）───────────────────────────────────
    var currentTab by mutableStateOf(0)
        private set

    // ── 派生属性（读 compose state，自动触发重组）───────────
    val chinese:          Boolean get() = isOn(Feature.Language)
    val toastEnabled:     Boolean get() = isOn(Feature.Toast)
    val arrayListEnabled: Boolean get() = isOn(Feature.ArrayList)

    init {
        // 默认开启的两个
        _toggles[Feature.ArrayList] = true
        _toggles[Feature.Toast]     = true
        activeFeatures.add(Feature.ArrayList)

        // 恢复上次的 Tab
        runCatching {
            currentTab = ModuleAssets.prefs()?.getInt(KEY_TAB, 0) ?: 0
        }
    }

    // ═════════════════════════════════════════════════════════
    //  查询
    // ═════════════════════════════════════════════════════════

    fun isOn(f: Feature): Boolean = _toggles[f] == true

    // ═════════════════════════════════════════════════════════
    //  修改
    // ═════════════════════════════════════════════════════════

    fun toggle(f: Feature) {
        if (f.kind != Feature.Kind.Toggle) return
        set(f, !isOn(f))
    }

    fun set(f: Feature, on: Boolean) {
        if (f.kind != Feature.Kind.Toggle) return
        _toggles[f] = on

        // 真 Hook 的功能
        when (f) {
            Feature.Multiplier -> HookBridge.multiplier = if (on) multiplierValue else 1
            Feature.AutoTap    -> if (on) HookBridge.startAutoTap(autoTapInterval)
                                   else    HookBridge.stopAutoTap()
            else               -> Unit
        }

        if (on) { if (f !in activeFeatures) activeFeatures.add(f) }
        else    { activeFeatures.remove(f) }
    }

    /** 执行一次性动作；返回是否成功。 */
    fun run(f: Feature): Boolean = when (f) {
        Feature.OneKeyClear -> HookBridge.oneKeyClear()
        Feature.Reset       -> HookBridge.resetCount()
        else                -> false
    }

    // ═════════════════════════════════════════════════════════
    //  Tab
    // ═════════════════════════════════════════════════════════

    fun selectTab(i: Int) {
        if (i == currentTab) return
        currentTab = i
        runCatching {
            ModuleAssets.prefs()?.edit()?.putInt(KEY_TAB, i)?.apply()
        }
    }

    // ═════════════════════════════════════════════════════════
    //  本地化
    // ═════════════════════════════════════════════════════════

    fun label(f: Feature): String = if (chinese) f.labelZh else f.labelEn
    fun desc(f: Feature):  String = if (chinese) f.descZh  else f.descEn

    // 兼容旧字符串调用（FloatToast 还在用）
    fun label(name: String): String = Feature.of(name)?.let { label(it) } ?: name
    fun desc(name: String):  String = Feature.of(name)?.let { desc(it) }  ?: ""

    /** @deprecated 用 [label] 代替。 */
    @Deprecated("Use label()", ReplaceWith("label(name)"))
    fun trans(name: String): String = label(name)
}
