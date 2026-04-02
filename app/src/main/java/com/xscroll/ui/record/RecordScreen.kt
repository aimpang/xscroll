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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

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
            .background(Color.Black),
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
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Uploading...", color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            RecordPhase.DONE -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Posted!", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                            color = Color.Red,
                            fontSize = 14.sp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Tap to retry",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { viewModel.retake() },
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
    Box(
        modifier = modifier
            .size(72.dp)
            .border(3.dp, Color.White, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(Color.Red)
            .clickable { onTap() },
    )
}

@Composable
private fun RecordingProgress(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            color = Color.Red,
            trackColor = Color.White.copy(alpha = 0.3f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = String.format("%.1fs", progress.value * 3),
            color = Color.White,
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { onRetake() }
                .padding(horizontal = 32.dp, vertical = 12.dp),
        ) {
            Text("Retake", color = Color.White, fontSize = 16.sp)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .clickable { onConfirm() }
                .padding(horizontal = 32.dp, vertical = 12.dp),
        ) {
            Text("Post", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CloseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text("\u2715", color = Color.White, fontSize = 18.sp)
    }
}
