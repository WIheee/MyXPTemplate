# ─────────────────────────────────────────────────────────────
#  Compose 需要的注解
# ─────────────────────────────────────────────────────────────
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# ─────────────────────────────────────────────────────────────
#  Stellar
# ─────────────────────────────────────────────────────────────
-keep class roro.stellar.** { *; }
-dontwarn roro.stellar.**
