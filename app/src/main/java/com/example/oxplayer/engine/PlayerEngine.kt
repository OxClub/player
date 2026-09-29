package com.example.oxplayer.engine

import android.net.Uri
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout

/**
 * Wraps a single libVLC MediaPlayer.
 * Handles: loading any file/URI, video<->audio-only switching, seek, speed.
 */
class PlayerEngine(private val libVLC: LibVLC) {

    val mediaPlayer: MediaPlayer = MediaPlayer(libVLC)

    private var videoLayout: VLCVideoLayout? = null
    private var isAudioOnly = false

    /** Call once when the Compose screen's VLCVideoLayout is created. */
    fun attachVideoOutput(layout: VLCVideoLayout) {
        videoLayout = layout
        mediaPlayer.attachViews(layout, null, false, false)
    }

    fun detachVideoOutput() {
        mediaPlayer.detachViews()
        videoLayout = null
    }

    fun load(uri: Uri, startPositionMs: Long = 0L, startAudioOnly: Boolean = false) {
        val media = Media(libVLC, uri).apply {
            setHWDecoderEnabled(true, false)
            addOption(":network-caching=1500")
        }
        mediaPlayer.media = media
        media.release()
        isAudioOnly = startAudioOnly
        mediaPlayer.play()
        if (startPositionMs > 0) mediaPlayer.time = startPositionMs
        if (startAudioOnly) disableVideoTrack()
    }

    /** "Play as audio": stop decoding the video track, keep the audio track running. */
    fun setAudioOnly(enabled: Boolean) {
        isAudioOnly = enabled
        if (enabled) disableVideoTrack() else enableVideoTrack()
    }

    fun toggleAudioOnly(): Boolean {
        setAudioOnly(!isAudioOnly)
        return isAudioOnly
    }

    fun isAudioOnly(): Boolean = isAudioOnly

    private fun disableVideoTrack() {
        // -1 tells libVLC to stop rendering/decoding video; audio keeps playing.
        mediaPlayer.setVideoTrackEnabled(false)
    }

    private fun enableVideoTrack() {
        mediaPlayer.setVideoTrackEnabled(true)
    }

    fun seekTo(ms: Long) {
        mediaPlayer.time = ms
    }

    fun seekBy(deltaMs: Long) {
        val target = (mediaPlayer.time + deltaMs).coerceIn(0, mediaPlayer.length)
        mediaPlayer.time = target
    }

    fun setSpeed(rate: Float) {
        mediaPlayer.rate = rate
    }

    fun togglePlayPause() {
        if (mediaPlayer.isPlaying) mediaPlayer.pause() else mediaPlayer.play()
    }

    fun release() {
        detachVideoOutput()
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}
