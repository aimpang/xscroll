package com.xscroll.ui.record

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File

class CameraController(private val context: Context) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    var previewView: PreviewView? = null
        private set

    fun initialize(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        onReady: () -> Unit,
    ) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            cameraProvider = future.get()

            val preview = Preview.Builder().build()
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            previewView = PreviewView(context).also {
                preview.surfaceProvider = it.surfaceProvider
            }

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture,
            )

            onReady()
        }, ContextCompat.getMainExecutor(context))
    }

    fun startRecording(
        onFinished: (Uri) -> Unit,
        onError: (String) -> Unit,
    ) {
        val capture = videoCapture ?: run {
            onError("Camera not ready")
            return
        }

        val file = File(context.cacheDir, "xscroll_${System.currentTimeMillis()}.mp4")
        val outputOptions = FileOutputOptions.Builder(file).build()

        activeRecording = capture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        if (event.hasError()) {
                            onError("Recording failed: ${event.cause?.message}")
                        } else {
                            onFinished(Uri.fromFile(file))
                        }
                    }
                }
            }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    fun release() {
        activeRecording?.stop()
        cameraProvider?.unbindAll()
    }
}

@Composable
fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(controller) {
        controller.initialize(lifecycleOwner) {}
        onDispose { controller.release() }
    }

    controller.previewView?.let { view ->
        AndroidView(
            factory = { view },
            modifier = modifier,
        )
    }
}
