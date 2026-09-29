package com.example.media

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

data class PlaybackState(
    val messageId: Long? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L
)

class AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun playAudio(messageId: Long, filePath: String, totalDurationMs: Long = 0L) {
        val file = File(filePath)
        if (!file.exists()) {
            return
        }

        if (_playbackState.value.messageId == messageId && mediaPlayer != null) {
            if (_playbackState.value.isPlaying) {
                mediaPlayer?.pause()
                _playbackState.value = _playbackState.value.copy(isPlaying = false)
                progressJob?.cancel()
            } else {
                mediaPlayer?.start()
                _playbackState.value = _playbackState.value.copy(isPlaying = true)
                startProgressTracker()
            }
            return
        }

        // Switching or new playback
        stopAudio()

        try {
            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    _playbackState.value = PlaybackState(
                        messageId = messageId,
                        isPlaying = false,
                        currentPositionMs = 0L,
                        durationMs = it.duration.toLong()
                    )
                    progressJob?.cancel()
                }
            }
            mediaPlayer = player
            player.start()
            val duration = if (player.duration > 0) player.duration.toLong() else totalDurationMs
            _playbackState.value = PlaybackState(
                messageId = messageId,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = duration
            )
            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
            stopAudio()
        }
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && mediaPlayer?.isPlaying == true) {
                val current = mediaPlayer?.currentPosition?.toLong() ?: 0L
                val dur = mediaPlayer?.duration?.toLong() ?: _playbackState.value.durationMs
                _playbackState.value = _playbackState.value.copy(
                    currentPositionMs = current,
                    durationMs = dur
                )
                delay(100)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _playbackState.value = _playbackState.value.copy(currentPositionMs = positionMs)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAudio() {
        progressJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _playbackState.value = PlaybackState()
        }
    }

    fun release() {
        stopAudio()
    }
}
