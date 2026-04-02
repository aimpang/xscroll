package com.xscroll.ui.feed

import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ClippingMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUri: Uri,
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val player = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1f
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("VideoPlayer", "Playback error for $videoUri: ${error.message}", error)
                }
                override fun onPlaybackStateChanged(state: Int) {
                    val stateName = when (state) {
                        Player.STATE_IDLE -> "IDLE"
                        Player.STATE_BUFFERING -> "BUFFERING"
                        Player.STATE_READY -> "READY"
                        Player.STATE_ENDED -> "ENDED"
                        else -> "UNKNOWN"
                    }
                    Log.d("VideoPlayer", "State: $stateName for $videoUri")
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    Log.d("VideoPlayer", "Video size: ${videoSize.width}x${videoSize.height}")
                }
                override fun onRenderedFirstFrame() {
                    Log.d("VideoPlayer", "First frame rendered for $videoUri")
                }
            })

            val mediaSourceFactory = DefaultMediaSourceFactory(context)
            val baseSource = mediaSourceFactory.createMediaSource(MediaItem.fromUri(videoUri))
            val clipped = ClippingMediaSource(baseSource, 0L, 3_000_000L) // 3 seconds in microseconds
            setMediaSource(clipped)
            prepare()
            Log.d("VideoPlayer", "Player prepared for: $videoUri")
        }
    }

    LaunchedEffect(isActive) {
        player.playWhenReady = isActive
    }

    DisposableEffect(videoUri) {
        onDispose {
            Log.d("VideoPlayer", "Releasing player for: $videoUri")
            player.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                this.player = player
            }
        },
        modifier = modifier,
        update = { view -> view.player = player },
    )
}
