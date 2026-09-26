package com.myxptemplate.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.util.Log

/**
 * 悬浮层的资源访问入口。
 *
 * 悬浮层被注入到目标应用进程后，LocalContext 是目标应用，它的 AssetManager
 * 里并没有本模块的 assets（pig.gif 等）。这里统一改成从模块自身的 APK 读取。
 *
 * 同时暴露模块自身 Context，供 [prefs] 做轻量持久化（如 Tab 记忆）。
 */
object ModuleAssets {

    private const val TAG = "MyXPTemplate"
    private const val PREF_NAME = "myxp_prefs"

    @Volatile
    private var assets: AssetManager? = null

    @Volatile
    private var moduleContext: Context? = null

    /** 注入悬浮层前调用一次；host 用当前进程里任意可用的 Context。 */
    fun init(host: Context, modulePackage: String) {
        if (assets != null) return
        val ctx = runCatching {
            host.createPackageContext(modulePackage, Context.CONTEXT_IGNORE_SECURITY)
        }.onFailure {
            Log.e(TAG, "初始化模块 assets 失败: $modulePackage", it)
        }.getOrNull()
        moduleContext = ctx
        assets = ctx?.assets
    }

    fun isReady(): Boolean = assets != null

    /** 模块自身的 Context（未初始化前返回 null）。 */
    fun context(): Context? = moduleContext

    /** 模块私有的 SharedPreferences（未初始化前返回 null）。 */
    fun prefs(): SharedPreferences? =
        moduleContext?.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /** 从模块 APK 的 assets 里解码资源；失败返回 null，由调用方兜底。 */
    fun loadDrawable(name: String): Drawable? {
        val am = assets ?: return null
        return runCatching {
            val src = ImageDecoder.createSource(am, name)
            ImageDecoder.decodeDrawable(src).also { d ->
                if (d is AnimatedImageDrawable) d.start()
            }
        }.onFailure {
            Log.w(TAG, "读取模块资源失败: $name", it)
        }.getOrNull()
    }
}
