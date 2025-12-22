package com.example.maapp.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.maapp.network.Player

class MediaSessionManager(context: Context) {

    private val player = ExoPlayer.Builder(context).build().apply {
        playWhenReady = false
    }

    private val mediaSession: MediaSession = MediaSession.Builder(context, this.player).build()

    fun updateFromPlayer(playerState: Player?) {
        val metadata = MediaMetadata.Builder()
            .setTitle(playerState?.currentItem?.title ?: "Nothing playing")
            .setArtist(playerState?.currentItem?.artist)
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(playerState?.playerId ?: "unknown")
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
    }

    fun release() {
        mediaSession.release()
        player.release()
    }
}
