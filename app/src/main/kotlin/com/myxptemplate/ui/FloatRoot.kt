package com.myxptemplate.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.myxptemplate.data.FeatureStore

@Composable
fun FloatRoot(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        FloatHud()

        AnimatedVisibility(
            visible = !FeatureStore.menuVisible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(150))
        ) {
            FloatIcon()
        }

        AnimatedVisibility(
            visible = FeatureStore.menuVisible,
            enter = fadeIn(tween(220)) + scaleIn(tween(280), initialScale = 0.92f),
            exit = fadeOut(tween(180)) + scaleOut(tween(180), targetScale = 0.92f)
        ) {
            FloatMenu()
        }

        FloatToastHost()
    }
}