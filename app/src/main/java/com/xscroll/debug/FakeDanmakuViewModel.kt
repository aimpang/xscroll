package com.xscroll.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xscroll.ui.danmaku.DanmakuState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FakeDanmakuViewModel : ViewModel() {

    private val _state = MutableStateFlow(DanmakuState(tokenCount = 99))
    val state: StateFlow<DanmakuState> = _state.asStateFlow()

    fun observeVideo(videoId: String) {
        val messages = FakeVideos.danmakuByVideo[videoId] ?: emptyList()
        _state.update { it.copy(messages = messages) }
    }

    fun showInput() { _state.update { it.copy(isInputVisible = true) } }
    fun hideInput() { _state.update { it.copy(isInputVisible = false) } }

    fun onLoopCountChanged(loopCount: Int) {
        _state.update { it.copy(isMessagingLocked = loopCount >= 3) }
    }

    fun sendMessage(text: String, loopCount: Int) {
        if (loopCount >= 3) return
        val newMsg = com.xscroll.data.model.DanmakuMessage(
            id = "local_${System.currentTimeMillis()}",
            text = text,
            color = "#FFFFFF",
            timestampMs = loopCount * 3000 + (0 until 3000).random(),
            userId = "debug_user",
        )
        _state.update {
            it.copy(
                messages = it.messages + newMsg,
                isInputVisible = false,
            )
        }
    }
}
