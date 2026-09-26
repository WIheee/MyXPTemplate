# ─────────────────────────────────────────────────────────────
#  JNI 名字绑定保护
# ─────────────────────────────────────────────────────────────
#  NativeBridge 里的 external 方法名与 native_bridge.cpp 里的
#  Java_com_myxptemplate_util_NativeBridge_xxx 一一对应。
#  混淆会改方法名 → JNI 找不到 → UnsatisfiedLinkError。
#
#  keepclasseswithmembernames 只保留「含 native 方法的类」的
#  方法名，不影响其他方法；体积增量几乎为 0。
-keepclasseswithmembernames class com.myxptemplate.util.NativeBridge {
    native <methods>;
}

# ─────────────────────────────────────────────────────────────
#  Xposed 入口 / 反射桥
# ─────────────────────────────────────────────────────────────
-keep class com.myxptemplate.xposed.Module { *; }
-keep class com.myxptemplate.xposed.HookBridge { *; }
-keep class com.myxptemplate.ModuleConfig { *; }

# ─────────────────────────────────────────────────────────────
#  Compose 需要的注解
# ─────────────────────────────────────────────────────────────
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
