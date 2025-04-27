package com.example.taxi.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import com.example.taxi.R

class SoundPlayer(private val soundType: SoundType, private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager


    fun playSound() {

        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)


        val soundResId = when (soundType) {
            SoundType.LowSound -> R.raw.alif_message
            SoundType.MiddleSound -> R.raw.alif_message
            SoundType.RequestSound -> R.raw.alif_request
        }

        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.isLooping = false
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.start()
    }

    fun playRequestSound(){
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)
        val soundResId = R.raw.alif_request
        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(1.0f, 1.0f)
        mediaPlayer?.start()
    }

    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    enum class SoundType {
        LowSound,
        MiddleSound,
        RequestSound
    }


}