package com.myxptemplate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myxptemplate.data.FeatureStore
import kotlinx.coroutines.delay

data class ToastItem(val id: Long, val title: String, val desc: String, val on: Boolean)

object ToastBus {
    val items = mutableStateListOf<ToastItem>()
    private var nextId = 0L

    fun push(title: String, desc: String, on: Boolean) {
        if (!FeatureStore.toastEnabled) return
        val id = nextId++
        items.add(ToastItem(id, title, desc, on))
        if (items.size > 3) items.removeAt(0)
    }

    fun remove(id: Long) {
        items.removeAll { it.id == id }
    }
}

fun FeatureStore.pushToast(key: String, on: Boolean, isAction: Boolean = false) {
    if (!toastEnabled) return
    val desc = if (isAction) {
        if (chinese) (if (on) "已完成" else "失败") else (if (on) "Done" else "Failed")
    } else {
        if (chinese) (if (on) "已开启" else "已关闭") else (if (on) "Enabled" else "Disabled")
    }
    ToastBus.push(trans(key), desc, on)
}

@Composable
fun FloatToastHost() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(
            Modifier
                .padding(end = 12.dp, bottom = 16.dp)
                .width(220.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ToastBus.items.forEach { item ->
                key(item.id) { ToastCardHost(item) }
            }
        }
    }
}

@Composable
private fun ToastCardHost(item: ToastItem) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        visible = false
        delay(260)
        ToastBus.remove(item.id)
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(260)) + slideInHorizontally(tween(260)) { it },
        exit = fadeOut(tween(220)) + slideOutHorizontally(tween(220)) { it }
    ) {
        ToastCard(item)
    }
}

@Composable
private fun ToastCard(item: ToastItem) {
    Row(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xF21C1C1C))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(width = 3.dp, height = 24.dp)
                .background(if (item.on) Color(0xFF9C5F54) else Color(0xFF555555))
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(item.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(item.desc, color = Color(0xFF888888), fontSize = 11.sp)
        }
    }
}
