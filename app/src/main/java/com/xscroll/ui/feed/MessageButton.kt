package com.xscroll.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xscroll.ui.theme.LocalXScrollColors

@Composable
fun MessageButton(
    tokenCount: Int,
    isLocked: Boolean,
    secondsOnVideo: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val accent = LocalXScrollColors.current

    // Final 9s window is three 3s loops; show 3, 2, 1 in the last three seconds (6s–9s)
    val countdownSec = if (secondsOnVideo in 6..8) 9 - secondsOnVideo else null
    val showCountdown = countdownSec != null && !isLocked

    // Subtle idle bounce
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scale",
    )

    AnimatedVisibility(
        visible = !isLocked,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (showCountdown) accent.countdown.copy(alpha = 0.25f)
                    else colors.onBackground.copy(alpha = 0.12f)
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics {
                    contentDescription = if (showCountdown) "Comment closing in $countdownSec" else "Say something stupid, $tokenCount tokens"
                    role = Role.Button
                },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showCountdown) "\uD83E\uDD2A" else "\uD83E\uDEE0",
                    fontSize = 14.sp,
                )
                Text(
                    text = if (countdownSec != null) " $countdownSec" else " say something stupid",
                    color = if (showCountdown) accent.countdown else accent.textDimmed,
                    fontSize = 12.sp,
                    fontWeight = if (showCountdown) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    text = "  $tokenCount\u2726",
                    color = accent.gold.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
