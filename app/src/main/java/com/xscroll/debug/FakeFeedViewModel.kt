package com.xscroll.debug

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xscroll.ui.feed.FeedState
import com.xscroll.ui.feed.VideoItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FakeFeedViewModel : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(300) // simulate load
            val items = FakeVideos.videos.map { video ->
                VideoItem(video = video, downloadUri = Uri.parse(video.storageUrl))
            }
            Log.d("FakeFeed", "Loaded ${items.size} videos, uris: ${items.map { it.downloadUri }}")
            _state.update { it.copy(videos = items, isLoading = false) }
        }
    }

    fun onPageChanged(index: Int) {
        _state.update { it.copy(currentIndex = index) }
    }

    fun loadMore() {} // no-op for fake
}
