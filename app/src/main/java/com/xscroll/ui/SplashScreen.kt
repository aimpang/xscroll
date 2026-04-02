package com.xscroll.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    val scale = remember { Animatable(0.3f) }
    val rotation = remember { Animatable(-90f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Entrance: spin in + scale up + fade in together
        launch { scale.animateTo(1f, tween(600, easing = FastOutSlowInEasing)) }
        launch { rotation.animateTo(0f, tween(600, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(1f, tween(400, easing = LinearEasing)) }

        delay(1000)

        // Exit: scale up + fade out
        launch { scale.animateTo(1.5f, tween(300, easing = FastOutSlowInEasing)) }
        launch { alpha.animateTo(0f, tween(300, easing = LinearEasing)) }

        delay(300)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "X",
            color = colors.onBackground,
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                }
                .alpha(alpha.value),
        )
    }
}
