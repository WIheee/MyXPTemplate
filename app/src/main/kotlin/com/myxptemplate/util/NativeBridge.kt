package com.myxptemplate.util

import com.myxptemplate.ModuleConfig

/**
 * ════════════════════════════════════════════════════════════════
 *   Native 桥 —— Java API
 * ════════════════════════════════════════════════════════════════
 *
 *  ▸ 加载 libmyxp.so（失败时 [available] = false，上层自动回退 Java 反射）
 *  ▸ 所有方法都是 JNI 直调，无反射开销
 *
 *  JNI 名字绑定：下面的 external 方法名必须与 native_bridge.cpp
 *  里 `Java_com_myxptemplate_util_NativeBridge_xxx` 一一对应。
 *  proguard-rules.pro 里已加 keep 规则防止被混淆。
 */
object NativeBridge {

    /** 与 native_bridge.cpp 里 `enum class Method` 保持一致。 */
    object Method {
        const val REFRESH           = 0
        const val ON_TAP            = 1
        const val SHOW_CLEAR_DIALOG = 2
    }

    /** 加载失败自动 false，不抛异常。 */
    @Volatile
    var available: Boolean = false
        private set

    init {
        available = runCatching {
            System.loadLibrary("myxp")
            true
        }.getOrDefault(false)

        L.i { "NativeBridge.available = $available" }
    }

    // ═════════════════════════════════════════════════════════
    //  Native 方法
    // ═════════════════════════════════════════════════════════

    /** 缓存目标实例的类结构；只需在 attach 时调一次。 */
    external fun cacheTarget(act: Any): Boolean

    /** 写 count 字段；不触发 refresh。 */
    external fun setInt(act: Any, value: Int)

    /** 读 count 字段。 */
    external fun getInt(act: Any): Int

    /** 调用 void 方法；which 见 [Method]。 */
    external fun callVoid(act: Any, which: Int)

    /**
     * 一步到位：读 → +mult → 截断到 cap → 写 → refresh
     * 达到 cap 时自动 showClearDialog。
     * @return 写入后的 count；失败返回 -1
     */
    external fun applyMultiplier(act: Any, mult: Int, cap: Int): Int

    /** 释放缓存的全局引用；detach 时调。 */
    external fun release()
}
