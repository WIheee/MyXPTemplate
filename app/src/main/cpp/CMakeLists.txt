# ════════════════════════════════════════════════════════════════
#  MyXPTemplate Native 桥 —— 编译成 libmyxp.so
#  工具链：NDK 30.0.15729638 + CMake 4.1.2
# ════════════════════════════════════════════════════════════════
#
#  CMake 4.x 说明：
#    · cmake_minimum_required 最低可写 3.5
#      写 3.22.1 是为了拿到现代 target_* 语法
#      4.1.2 满足 >= 3.22.1，不会报错
#    · 4.x 默认启用了若干新策略（CMP0xxx）
#      用 cmake_policy 显式锁定，避免 warning 刷屏
#
cmake_minimum_required(VERSION 3.22.1)

# 锁定策略范围到 3.22（拿到现代语义又不引入 4.x 未定义行为）
cmake_policy(VERSION 3.22.1)

project(myxp LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# ── 库本体 ────────────────────────────────────────────────────
add_library(myxp SHARED native_bridge.cpp)

# ── 通用：符号隐藏 + 减小体积 ─────────────────────────────────
target_compile_options(myxp PRIVATE
    -fvisibility=hidden
    -ffunction-sections
    -fdata-sections
    -fno-exceptions
    -fno-rtti
    -Wall
    -Wextra
)

target_link_options(myxp PRIVATE
    -Wl,--gc-sections
    -Wl,--exclude-libs,ALL
)

# ── Release 专属：极致优化 ────────────────────────────────────
target_compile_options(myxp PRIVATE
    $<$<CONFIG:Release>:-O3>
    $<$<CONFIG:Release>:-flto>
    $<$<CONFIG:Release>:-fomit-frame-pointer>
)

target_link_options(myxp PRIVATE
    $<$<CONFIG:Release>:-flto>
)

# ── 只链 liblog ──────────────────────────────────────────────
find_library(log-lib log)
target_link_libraries(myxp ${log-lib})
