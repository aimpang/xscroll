package com.xscroll.ui.feed

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.DocumentSnapshot
import com.xscroll.data.model.Video
import com.xscroll.data.repository.UserRepository
import com.xscroll.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoItem(
    val video: Video,
    val downloadUri: Uri? = null,
)

data class FeedState(
    val videos: List<VideoItem> = emptyList(),
    val isLoading: Boolean = false,
    val currentIndex: Int = 0,
    val error: String? = null,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val videoRepository: VideoRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private var lastDocument: DocumentSnapshot? = null
    private var isLoadingMore = false

    init {
        viewModelScope.launch {
            userRepository.signInAnonymously()
            loadMore()
        }
    }

    fun loadMore() {
        if (isLoadingMore) return
        isLoadingMore = true

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val (videos, lastDoc) = videoRepository.getVideos(after = lastDocument)
                lastDocument = lastDoc

                val items = videos.map { video ->
                    val uri = try {
                        videoRepository.getDownloadUrl(video.storageUrl)
                    } catch (e: Exception) {
                        null
                    }
                    VideoItem(video = video, downloadUri = uri)
                }.filter { it.downloadUri != null }

                _state.update { current ->
                    current.copy(
                        videos = current.videos + items,
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun onPageChanged(index: Int) {
        _state.update { it.copy(currentIndex = index) }
        // Preload more when near the end
        val videos = _state.value.videos
        if (index >= videos.size - 3) {
            loadMore()
        }
    }

}
