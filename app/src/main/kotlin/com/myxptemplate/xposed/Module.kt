package com.myxptemplate.xposed

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.myxptemplate.ModuleConfig
import com.myxptemplate.ui.FloatRoot
import com.myxptemplate.ui.theme.ComposeEmptyActivityTheme
import com.myxptemplate.util.ModuleAssets
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class Module : XposedModule() {

    private val tag            = ModuleConfig.LOG_TAG
    private val targetPackage  = ModuleConfig.TARGET_PACKAGE
    private val targetActivity = ModuleConfig.TARGET_ACTIVITY
    private val modulePackage  = ModuleConfig.MODULE_PACKAGE
    private val overlayTag     = ModuleConfig.OVERLAY_TAG

    override fun onModuleLoaded(param: XposedModuleInterface.ModuleLoadedParam) {
        log(Log.INFO, tag, "模块已加载，进程: ${param.processName}")
    }

    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName != targetPackage) return
        log(Log.INFO, tag, "目标应用就绪: ${param.packageName}")
        hookActivityOnCreate(param.classLoader)
        hookActivityOnDestroy(param.classLoader)
        hookOnTap(param.classLoader)
    }

    private fun hookActivityOnCreate(classLoader: ClassLoader) {
        try {
            val activityCls = classLoader.loadClass("android.app.Activity")
            val onCreate = activityCls.getDeclaredMethod("onCreate", Bundle::class.java)

            hook(onCreate).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val result = chain.proceed()
                    val activity = chain.thisObject as? Activity ?: return result
                    if (activity.javaClass.name != targetActivity) return result

                    HookBridge.attach(activity)
                    injectOverlay(activity)
                    return result
                }
            })
        } catch (t: Throwable) {
            log(Log.ERROR, tag, "Hook Activity#onCreate 失败", t)
        }
    }

    private fun hookActivityOnDestroy(classLoader: ClassLoader) {
        try {
            val activityCls = classLoader.loadClass("android.app.Activity")
            val onDestroy = activityCls.getDeclaredMethod("onDestroy")

            hook(onDestroy).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val activity = chain.thisObject as? Activity
                    // 只在销毁的正是当前持有的那个 Activity 时才 detach
                    if (activity?.javaClass?.name == targetActivity &&
                        HookBridge.isCurrent(activity)) {
                        HookBridge.detach()
                    }
                    return chain.proceed()
                }
            })
        } catch (t: Throwable) {
            log(Log.ERROR, tag, "Hook Activity#onDestroy 失败", t)
        }
    }

    private fun hookOnTap(classLoader: ClassLoader) {
        try {
            val clazz = classLoader.loadClass(targetActivity)
            val onTap = clazz.getDeclaredMethod(ModuleConfig.METHOD_ON_TAP)

            hook(onTap).intercept(object : XposedInterface.Hooker {
                override fun intercept(chain: XposedInterface.Chain): Any? {
                    val mult = HookBridge.multiplier
                    return if (mult <= 1) {
                        chain.proceed()
                    } else {
                        HookBridge.applyMultiplier(chain.thisObject, mult)
                        null
                    }
                }
            })
        } catch (t: Throwable) {
            log(Log.ERROR, tag, "Hook onTap 失败", t)
        }
    }

    /**
     * Compose 的 WindowRecomposer 会从 parent 开始向上查找 ViewTreeLifecycleOwner。
     * 只要 owner 设置在 DecorView 上，无论 ComposeView 的 parent 是谁都能找到。
     * 三重设置只是为了兜底。
     */
    private fun injectOverlay(activity: Activity) {
        try {
            val decor = activity.window.decorView as? ViewGroup ?: return
            if (decor.findViewWithTag<View>(overlayTag) != null) return

            try {
                ModuleAssets.init(activity, modulePackage)
            } catch (t: Throwable) {
                log(Log.ERROR, tag, "初始化模块 assets 失败", t)
            }

            val host = OverlayHost()
            val lifecycleOwner: LifecycleOwner = activity as? LifecycleOwner ?: host
            val savedStateOwner: SavedStateRegistryOwner =
                activity as? SavedStateRegistryOwner ?: host
            val viewModelStoreOwner: ViewModelStoreOwner =
                activity as? ViewModelStoreOwner ?: host

            // ① DecorView 兜底
            decor.setViewTreeLifecycleOwner(lifecycleOwner)
            decor.setViewTreeSavedStateRegistryOwner(savedStateOwner)
            decor.setViewTreeViewModelStoreOwner(viewModelStoreOwner)

            // ② 容器
            val container = FrameLayout(activity).apply {
                tag = overlayTag
                isClickable = false
                isFocusable = false
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(savedStateOwner)
                setViewTreeViewModelStoreOwner(viewModelStoreOwner)
            }

            // ③ ComposeView 三重保险
            val cv = ComposeView(activity).apply {
                isClickable = false
                isFocusable = false
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(savedStateOwner)
                setViewTreeViewModelStoreOwner(viewModelStoreOwner)
                setContent {
                    ComposeEmptyActivityTheme {
                        FloatRoot()
                    }
                }
            }

            container.addView(
                cv,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            decor.addView(
                container,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )

            log(Log.INFO, tag, "悬浮层已注入")
        } catch (t: Throwable) {
            log(Log.ERROR, tag, "注入悬浮层失败", t)
        }
    }
}

/**
 * 目标 Activity 不是 LifecycleOwner / SavedStateRegistryOwner / ViewModelStoreOwner
 * 时给 ComposeView 用的兜底宿主。三者缺一，新版 Compose 都会抛异常。
 */
private class OverlayHost : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val vmStore = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = vmStore

    init {
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }
}
