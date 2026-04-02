package com.xscroll.ui.record

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import com.xscroll.ui.theme.LocalXScrollColors
import kotlinx.coroutines.delay
import java.util.Locale

private const val RECORD_DURATION_MS = 3000L

@Composable
fun RecordScreen(
    onDone: () -> Unit,
    onClose: () -> Unit,
    viewModel: RecordViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val cameraController = remember { CameraController(context) }
    val colors = MaterialTheme.colorScheme
    val accent = LocalXScrollColors.current

    // Auto-navigate on done
    LaunchedEffect(state.phase) {
        if (state.phase == RecordPhase.DONE) {
            delay(500)
            viewModel.reset()
            onDone()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        when (state.phase) {
            RecordPhase.IDLE -> {
                CameraPreview(
                    controller = cameraController,
                    modifier = Modifier.fillMaxSize(),
                )
                RecordButton(
                    onTap = {
                        viewModel.onRecordingStarted()
                        cameraController.startRecording(
                            onFinished = { uri -> viewModel.onRecordingFinished(uri) },
                            onError = { msg -> viewModel.onRecordingError(msg) },
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                )
                CloseButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                )
            }

            RecordPhase.RECORDING -> {
                CameraPreview(
                    controller = cameraController,
                    modifier = Modifier.fillMaxSize(),
                )
                RecordingProgress(
                    onComplete = { cameraController.stopRecording() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp),
                )
            }

            RecordPhase.PREVIEW -> {
                // Show preview with confirm/retake
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.recordedUri != null) {
                        com.xscroll.ui.feed.VideoPlayer(
                            videoUri = state.recordedUri!!,
                            isActive = true,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    PreviewControls(
                        onRetake = {
                            viewModel.retake()
                        },
                        onConfirm = {
                            viewModel.upload()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 48.dp),
                    )
                }
            }

            RecordPhase.UPLOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = colors.onBackground)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uploading...", color = colors.onBackground, fontSize = 14.sp)
                    }
                }
            }

            RecordPhase.DONE -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Posted!", color = colors.onBackground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            RecordPhase.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.error ?: "Something went wrong",
                            color = colors.error,
                            fontSize = 14.sp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tap to retry",
                            color = colors.onBackground,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clickable { viewModel.retake() }
                                .semantics {
                                    contentDescription = "Retry recording"
                                    role = Role.Button
                                },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordButton(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .size(72.dp)
            .border(3.dp, colors.onBackground, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(colors.error)
            .clickable { onTap() }
            .semantics {
                contentDescription = "Start recording"
                role = Role.Button
            },
    )
}

@Composable
private fun RecordingProgress(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = RECORD_DURATION_MS.toInt(),
                easing = LinearEasing,
            ),
        )
        onComplete()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LinearProgressIndicator(
            progress = { progress.value },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(4.dp),
            color = colors.error,
            trackColor = colors.onBackground.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format(Locale.US, "%.1fs", progress.value * 3),
            color = colors.onBackground,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun PreviewControls(
    onRetake: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colors.onBackground.copy(alpha = 0.2f))
                .clickable { onRetake() }
                .padding(horizontal = 32.dp, vertical = 12.dp)
                .semantics { role = Role.Button },
        ) {
            Text("Retake", color = colors.onBackground, fontSize = 16.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(colors.onBackground)
                .clickable { onConfirm() }
                .padding(horizontal = 32.dp, vertical = 12.dp)
                .semantics { role = Role.Button },
        ) {
            Text("Post", color = colors.background, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = LocalXScrollColors.current
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(accent.overlayDark)
            .clickable { onClick() }
            .semantics {
                contentDescription = "Close"
                role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        Text("\u2715", color = colors.onBackground, fontSize = 18.sp)
    }
}
