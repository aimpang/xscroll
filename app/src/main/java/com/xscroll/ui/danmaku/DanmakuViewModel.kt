package com.xscroll.ui.danmaku

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xscroll.data.model.DanmakuMessage
import com.xscroll.data.repository.DanmakuRepository
import com.xscroll.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DanmakuState(
    val messages: List<DanmakuMessage> = emptyList(),
    val tokenCount: Int = 0,
    val isInputVisible: Boolean = false,
    val isMessagingLocked: Boolean = false,
    val sendError: String? = null,
)

@HiltViewModel
class DanmakuViewModel @Inject constructor(
    private val danmakuRepository: DanmakuRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DanmakuState())
    val state: StateFlow<DanmakuState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var currentVideoId: String? = null

    fun observeVideo(videoId: String) {
        if (videoId == currentVideoId) return
        currentVideoId = videoId

        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            danmakuRepository.observeDanmaku(videoId).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }

        refreshTokenCount()
    }

    fun showInput() {
        _state.update { it.copy(isInputVisible = true) }
    }

    fun hideInput() {
        _state.update { it.copy(isInputVisible = false) }
    }

    fun onLoopCountChanged(loopCount: Int) {
        _state.update { it.copy(isMessagingLocked = loopCount >= 3) }
    }

    fun sendMessage(text: String, loopCount: Int) {
        if (loopCount >= 3) return
        val videoId = currentVideoId ?: return
        val uid = userRepository.getCurrentUid() ?: return

        viewModelScope.launch {
            val deducted = userRepository.deductToken(uid)
            if (!deducted) {
                _state.update { it.copy(sendError = "No tokens remaining") }
                return@launch
            }

            val message = DanmakuMessage(
                text = text,
                userId = uid,
                timestampMs = loopCount * 3000 + (0 until 3000).random(),
            )

            try {
                danmakuRepository.sendDanmaku(videoId, message)
                refreshTokenCount()
            } catch (e: Exception) {
                _state.update { it.copy(sendError = e.message) }
            }
        }
    }

    private fun refreshTokenCount() {
        val uid = userRepository.getCurrentUid() ?: return
        viewModelScope.launch {
            val user = userRepository.getUser(uid)
            _state.update { it.copy(tokenCount = user?.tokens ?: 0) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        observeJob?.cancel()
    }
}
