package com.xscroll.player

import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerPool @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val players = mutableMapOf<String, ExoPlayer>()

    @OptIn(UnstableApi::class)
    fun getOrCreate(videoId: String, videoUri: Uri): ExoPlayer {
        return players.getOrPut(videoId) {
            ExoPlayer.Builder(context).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1f
                setMediaItem(MediaItem.fromUri(videoUri))
                prepare()
            }
        }
    }

    fun play(videoId: String) {
        players[videoId]?.play()
    }

    fun pause(videoId: String) {
        players[videoId]?.pause()
    }

    fun pauseAll() {
        players.values.forEach { it.pause() }
    }

    fun release(videoId: String) {
        players.remove(videoId)?.release()
    }

    fun releaseAll() {
        players.values.forEach { it.release() }
        players.clear()
    }

    fun evictExcept(keepIds: Set<String>) {
        val toRemove = players.keys - keepIds
        toRemove.forEach { release(it) }
    }
}
