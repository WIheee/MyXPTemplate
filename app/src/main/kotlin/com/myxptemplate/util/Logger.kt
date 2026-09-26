package com.myxptemplate.util

import android.util.Log
import com.myxptemplate.ModuleConfig

/**
 * ════════════════════════════════════════════════════════════════
 *   惰性日志 —— 关掉时零开销，开启时一行到位
 * ════════════════════════════════════════════════════════════════
 *
 *  ▸ 惰性求值：只有真的输出才会拼字符串
 *      L.d { "count = $count  delta = ${System.nanoTime()}" }
 *
 *  ▸ 一行兜底：try/catch + 打日志 + 返回 null
 *      val n = L.attempt("read count") { act.getIntField("count") }
 *
 *  ▸ 计时打点：
 *      L.timed("inject overlay") { injectOverlay(activity) }
 *
 *  实现细节（两条都是 Kotlin 编译器对 inline 的硬性约束）：
 *
 *  1) inline 函数会展开到调用处，所以它引用的任何符号都必须在调用处
 *     可达。TAG 这种 private 字段会直接编译失败，所以直接用
 *     [ModuleConfig.LOG_TAG] —— 它是 public const val，编译期替换为
 *     字符串字面量，不存在符号访问问题。
 *
 *  2) inline 函数直接读 backing field（省一次 getter 调用），所以
 *     `enabled` 必须把 backing field 暴露为 public JVM 字段。
 *     加 @JvmField 就达到这个效果。
 */
object L {

    /** 关闭时所有日志调用退化为一次字段读 + 分支跳转。 */
    @JvmField
    @Volatile
    var enabled: Boolean = true

    inline fun d(msg: () -> String) {
        if (enabled) Log.d(ModuleConfig.LOG_TAG, msg())
    }

    inline fun i(msg: () -> String) {
        if (enabled) Log.i(ModuleConfig.LOG_TAG, msg())
    }

    inline fun w(msg: () -> String, t: Throwable? = null) {
        if (enabled) Log.w(ModuleConfig.LOG_TAG, msg(), t)
    }

    inline fun e(msg: () -> String, t: Throwable? = null) {
        if (enabled) Log.e(ModuleConfig.LOG_TAG, msg(), t)
    }

    /** try/catch + 打日志 + 返回 null。 */
    inline fun <T> attempt(what: String, block: () -> T): T? = try {
        block()
    } catch (t: Throwable) {
        e({ "$what 失败" }, t)
        null
    }

    /** 计时打点；无论成功失败都会输出耗时。 */
    inline fun <T> timed(what: String, block: () -> T): T {
        val start = System.nanoTime()
        try {
            return block()
        } finally {
            d { "$what 耗时 ${(System.nanoTime() - start) / 1_000_000.0}ms" }
        }
    }
}
