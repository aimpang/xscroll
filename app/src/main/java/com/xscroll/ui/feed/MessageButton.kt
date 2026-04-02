package com.xscroll.ui.feed

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    AnimatedVisibility(
        visible = !isLocked,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (showCountdown) accent.countdown.copy(alpha = 0.25f)
                    else colors.onBackground.copy(alpha = 0.12f)
                )
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .semantics {
                    contentDescription = if (showCountdown) "Comment closing in $countdownSec" else "Drop a comment, $tokenCount tokens"
                    role = Role.Button
                },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showCountdown) "⚡" else "💬",
                    fontSize = 14.sp,
                )
                Text(
                    text = if (countdownSec != null) " $countdownSec" else " drop a comment",
                    color = if (showCountdown) accent.countdown else accent.textDimmed,
                    fontSize = 12.sp,
                    fontWeight = if (showCountdown) FontWeight.Bold else FontWeight.Normal,
                )
                Text(
                    text = "  $tokenCount✦",
                    color = accent.gold.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
