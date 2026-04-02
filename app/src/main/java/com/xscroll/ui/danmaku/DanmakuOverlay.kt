package com.xscroll.ui.danmaku

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.xscroll.data.model.DanmakuMessage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlin.math.roundToInt

private const val MAX_VISIBLE = 30
private const val SCROLL_DURATION_MIN_MS = 2200
private const val SCROLL_DURATION_MAX_MS = 5000
private const val LOOP_DURATION_MS = 3000L
private const val NUM_LANES = 10
private const val INITIAL_DELAY_MS = 1000L

/**
 * [loopCount] increments every time the 3s video loop restarts.
 * Each message waits [DanmakuMessage.timestampMs] into the loop, then scrolls.
 * This keeps comments perfectly replay-synced to the video timeline.
 */
@Composable
fun DanmakuOverlay(
    messages: List<DanmakuMessage>,
    loopCount: Int,
    modifier: Modifier = Modifier,
) {
    val visible = messages.take(MAX_VISIBLE)

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val laneHeightPx = with(LocalDensity.current) { (maxHeight / NUM_LANES).toPx() }

        visible.forEachIndexed { index, msg ->
            key(msg.id) {
                val lane = (msg.text.hashCode() + index).mod(NUM_LANES)
                val yOffsetPx = laneHeightPx * lane

                DanmakuItem(
                    message = msg,
                    screenWidthPx = screenWidthPx,
                    yOffsetPx = yOffsetPx,
                    loopCount = loopCount,
                )
            }
        }
    }
}

@Composable
private fun DanmakuItem(
    message: DanmakuMessage,
    screenWidthPx: Float,
    yOffsetPx: Float,
    loopCount: Int,
) {
    val xOffset = remember { Animatable(screenWidthPx) }
    // Random speed per item instance — stable across recompositions
    val scrollDurationMs = remember { (SCROLL_DURATION_MIN_MS..SCROLL_DURATION_MAX_MS).random() }
    // rememberUpdatedState so the LaunchedEffect(Unit) always sees the latest loopCount
    val currentLoopCount = rememberUpdatedState(loopCount)

    // Fire exactly once when viewer reaches the target loop.
    // Keyed on message.id so it restarts when messages change (video swipe),
    // but loopCount increments (video re-looping) never cancel a mid-flight animation.
    LaunchedEffect(message.id) {
        val targetLoop = message.timestampMs / 3000
        val offsetMs = (message.timestampMs % 3000).toLong()

        // Wait for the first loop to give the viewer a moment to take in the video
        if (targetLoop == 0) {
            kotlinx.coroutines.delay(INITIAL_DELAY_MS)
        }

        snapshotFlow { currentLoopCount.value }
            .filter { it >= targetLoop }
            .first()

        xOffset.snapTo(screenWidthPx)
        kotlinx.coroutines.delay(offsetMs)
        xOffset.animateTo(
            targetValue = -screenWidthPx,
            animationSpec = tween(durationMillis = scrollDurationMs, easing = LinearEasing),
        )
    }

    val color = try {
        Color(android.graphics.Color.parseColor(message.color))
    } catch (e: Exception) {
        Color.White
    }

    Box(
        modifier = Modifier.offset {
            IntOffset(xOffset.value.roundToInt(), yOffsetPx.roundToInt())
        },
    ) {
        Text(
            text = message.text,
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Visible,
            style = androidx.compose.ui.text.TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.7f),
                    offset = Offset(1f, 1f),
                    blurRadius = 3f,
                ),
            ),
        )
    }
}
