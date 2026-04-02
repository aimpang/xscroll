package com.xscroll.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.Icon
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

    val countdownSec = if (secondsOnVideo in 6..8) 9 - secondsOnVideo else null
    val showCountdown = countdownSec != null && !isLocked

    AnimatedVisibility(
        visible = !isLocked,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .semantics {
                    contentDescription = if (showCountdown) "Comment closing in $countdownSec" else "Comment, $tokenCount tokens"
                    role = Role.Button
                },
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.onBackground.copy(alpha = 0.15f))
                    .clickable { onClick() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = null,
                    tint = colors.onBackground,
                    modifier = Modifier.size(26.dp),
                )
            }
            Text(
                text = if (showCountdown) "$countdownSec" else "$tokenCount",
                color = if (showCountdown) accent.countdown else colors.onBackground.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
