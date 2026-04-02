package com.xscroll.ui.record

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xscroll.data.repository.UserRepository
import com.xscroll.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class RecordPhase {
    IDLE,
    RECORDING,
    PREVIEW,
    UPLOADING,
    DONE,
    ERROR,
}

data class RecordState(
    val phase: RecordPhase = RecordPhase.IDLE,
    val recordedUri: Uri? = null,
    val progressMs: Long = 0,
    val error: String? = null,
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RecordState())
    val state: StateFlow<RecordState> = _state.asStateFlow()

    fun onRecordingStarted() {
        _state.update { it.copy(phase = RecordPhase.RECORDING, progressMs = 0) }
    }

    fun onRecordingProgress(ms: Long) {
        _state.update { it.copy(progressMs = ms) }
    }

    fun onRecordingFinished(uri: Uri) {
        _state.update { it.copy(phase = RecordPhase.PREVIEW, recordedUri = uri) }
    }

    fun onRecordingError(message: String) {
        _state.update { it.copy(phase = RecordPhase.ERROR, error = message) }
    }

    fun retake() {
        _state.update { RecordState() }
    }

    fun upload() {
        val uri = _state.value.recordedUri ?: return
        val uid = userRepository.getCurrentUid() ?: return

        _state.update { it.copy(phase = RecordPhase.UPLOADING) }

        viewModelScope.launch {
            try {
                videoRepository.uploadVideo(uid, uri)
                _state.update { it.copy(phase = RecordPhase.DONE) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(phase = RecordPhase.ERROR, error = e.message)
                }
            }
        }
    }

    fun reset() {
        _state.update { RecordState() }
    }
}
