package com.myxptemplate.overlay

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ════════════════════════════════════════════════════════════════
 *   悬浮窗全局状态
 * ════════════════════════════════════════════════════════════════
 *
 *   球的坐标一律是「屏幕绝对坐标」（px），渲染窗口始终全屏，
 *   所以 UI 侧直接用 Modifier.offset 摆放即可，与窗口 x/y 无关。
 *
 *   触摸则由 OverlayService 的触摸代理窗口负责，它每次布局变化都会
 *   通过 [onRelayout] 被通知：
 *
 *     ▸ 球模式（expanded = false）：代理窗口 = 球 + padding
 *         只有球区域可触摸，其余触摸直接穿透到下层 App。
 *
 *     ▸ 菜单模式（expanded = true）：全屏渲染窗口可触摸
 *         拦截所有触摸 —— 点击菜单外部关闭菜单。
 *
 *   注意：[expanded] 与 [moveBallBy] 都会触发 [onRelayout]，
 *   由 OverlayService 重算窗口 flags 与位置。
 */
object OverlayState {

    private val _expanded: MutableState<Boolean> = mutableStateOf(false)

    /** 是否展开菜单。写入时自动触发 onRelayout。 */
    var expanded: Boolean
        get() = _expanded.value
        set(value) {
            if (_expanded.value == value) return
            _expanded.value = value
            onRelayout?.invoke()
        }

    /** 球是否正在被拖拽（用来切换「奔跑」动画）。 */
    var dragging: Boolean by mutableStateOf(false)

    private val _ballX: MutableState<Int> = mutableStateOf(0)
    private val _ballY: MutableState<Int> = mutableStateOf(0)

    /** 球的左上角坐标（px，相对屏幕左上角）。 */
    val ballX: Int get() = _ballX.value
    val ballY: Int get() = _ballY.value

    /** 球边长（px）。 */
    var ballSize: Int = 0
        private set

    var screenWidth: Int = 0
        private set

    var screenHeight: Int = 0
        private set

    /** 布局变化时由 Service 注册的回调。 */
    var onRelayout: (() -> Unit)? = null

    fun init(screenW: Int, screenH: Int, size: Int, startX: Int, startY: Int) {
        screenWidth = screenW
        screenHeight = screenH
        ballSize = size
        _ballX.value = startX.coerceIn(0, (screenW - size).coerceAtLeast(0))
        _ballY.value = startY.coerceIn(0, (screenH - size).coerceAtLeast(0))
    }

    fun moveBallBy(dx: Float, dy: Float) {
        if (ballSize <= 0) return
        val maxX = (screenWidth - ballSize).coerceAtLeast(0)
        val maxY = (screenHeight - ballSize).coerceAtLeast(0)
        _ballX.value = (_ballX.value + dx).toInt().coerceIn(0, maxX)
        _ballY.value = (_ballY.value + dy).toInt().coerceIn(0, maxY)
        onRelayout?.invoke()
    }
}
