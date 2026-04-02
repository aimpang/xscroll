package com.xscroll.ui.danmaku

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xscroll.ui.theme.LocalXScrollColors

@Composable
fun DanmakuInput(
    visible: Boolean,
    tokenCount: Int,
    isLocked: Boolean,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val colors = MaterialTheme.colorScheme
    val accent = LocalXScrollColors.current

    LaunchedEffect(visible, isLocked) {
        if (visible && !isLocked) {
            text = ""
            focusRequester.requestFocus()
        }
        if (isLocked) onDismiss()
    }

    AnimatedVisibility(
        visible = visible && !isLocked,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= 50) text = it },
                modifier = Modifier
                    .weight(1f)
                    .background(colors.onBackground.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = "Message input" },
                textStyle = TextStyle(color = colors.onBackground, fontSize = 14.sp),
                cursorBrush = SolidColor(colors.onBackground),
                singleLine = true,
                decorationBox = { inner ->
                    if (text.isEmpty()) {
                        Text(
                            "Send a message...",
                            color = accent.textFaint,
                            fontSize = 14.sp,
                        )
                    }
                    inner()
                },
            )

            TextButton(
                onClick = {
                    if (text.isNotBlank() && tokenCount > 0) {
                        onSend(text.trim())
                        text = ""
                        onDismiss()
                    }
                },
                enabled = text.isNotBlank() && tokenCount > 0,
            ) {
                Text(
                    "Send",
                    color = if (text.isNotBlank() && tokenCount > 0)
                        colors.onBackground else colors.onBackground.copy(alpha = 0.3f),
                    fontSize = 14.sp,
                )
            }

            Text(
                text = "\uD83E\uDE99$tokenCount",
                color = colors.onBackground.copy(alpha = 0.5f),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}
