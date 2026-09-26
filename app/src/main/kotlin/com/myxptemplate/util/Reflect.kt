package com.myxptemplate.util

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * ════════════════════════════════════════════════════════════════
 *   一行式反射 —— 告别 getDeclaredField().apply{isAccessible=true}
 * ════════════════════════════════════════════════════════════════
 *
 *  以前：
 *      val f = act.javaClass.getDeclaredField("count").apply { isAccessible = true }
 *      f.setInt(act, 100)
 *
 *  现在：
 *      act.setIntField("count", 100)
 *
 *  所有函数内部都已 try/catch，失败返回 null 或静默，
 *  需要知道失败原因时用 L.attempt 包一层。
 */
object Reflect {

    // ── 底层 ────────────────────────────────────────────────

    fun Any.findField(name: String): Field =
        javaClass.getDeclaredField(name).apply { isAccessible = true }

    fun Any.findMethod(name: String, vararg params: Class<*>): Method =
        javaClass.getDeclaredMethod(name, *params).apply { isAccessible = true }

    // ── Int ─────────────────────────────────────────────────

    fun Any.getIntField(name: String): Int? =
        runCatching { findField(name).getInt(this) }.getOrNull()

    fun Any.setIntField(name: String, value: Int) {
        runCatching { findField(name).setInt(this, value) }
    }

    // ── 任意类型（泛型）─────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    fun <T> Any.getField(name: String): T? =
        runCatching { findField(name).get(this) as? T }.getOrNull()

    fun Any.setField(name: String, value: Any?) {
        runCatching { findField(name).set(this, value) }
    }

    // ── 方法调用 ────────────────────────────────────────────

    fun Any.callVoidMethod(name: String) {
        runCatching { findMethod(name).invoke(this) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> Any.callMethod(name: String, vararg args: Any?): T? =
        runCatching { findMethod(name).invoke(this, *args) as? T }.getOrNull()
}
