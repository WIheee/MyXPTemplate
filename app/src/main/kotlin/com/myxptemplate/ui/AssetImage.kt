package com.myxptemplate.ui

import android.graphics.drawable.AnimatedImageDrawable
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.viewinterop.AndroidView
import com.myxptemplate.util.ModuleAssets

/**
 * 从模块 APK 的 assets 里加载图片（png/gif 都支持）。
 * 缺失文件不会崩溃，自动 fallback 到 Material Icon。
 */
@Composable
fun AssetImage(
    name: String,
    modifier: Modifier = Modifier,
    fallback: String = "",
    contentDescription: String? = null,
    tint: Color = Color(0xFF9C5F54)
) {
    val drawable = remember(name) { ModuleAssets.loadDrawable(name) }
    if (drawable == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(
                imageVector = fallbackIcon(fallback),
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        AndroidView(
            factory = { c ->
                ImageView(c).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setImageDrawable(drawable)
                    (drawable as? AnimatedImageDrawable)?.start()
                }
            },
            modifier = modifier
        )
    }
}

private fun fallbackIcon(key: String): ImageVector = when (key) {
    "home" -> Icons.Filled.Home
    "combat" -> Icons.Filled.Bolt
    "move" -> Icons.Filled.DirectionsWalk
    "survival" -> Icons.Filled.Cloud
    "render" -> Icons.Filled.Speed
    else -> Icons.Filled.Extension
}
