package com.example.soundmodule

import android.content.Context
import android.media.MediaPlayer

class SoundManager(val context: Context) {


    private var mediaPlayer: MediaPlayer? = null

    private fun playSoundInternal(soundResId: Int) {
        // Release any existing MediaPlayer
        mediaPlayer?.release()

        // Create a new MediaPlayer for the given sound resource
        mediaPlayer = MediaPlayer.create(context, soundResId).apply {
            setOnCompletionListener {
                it.release()
            }
            start()
        }
    }

    fun playSoundYouAreOnline() {
        playSoundInternal(R.raw.siz_liniyadasiz)
    }


    fun playSoundFinish() {
//        playSoundInternal(R.raw.finish)
    }

    fun playSoundYouAreOffline() {
        playSoundInternal(R.raw.siz_oflaynsiz)
    }

    fun playSoundJourneyBeginWithBelt() {
        playSoundInternal(R.raw.safar_boshlandi_xavfsizlik_kamari)
    }

    fun playSoundLetsGo() {
        playSoundInternal(R.raw.kettik)
    }

    fun playSoundCancelOrder() {
//        playSoundInternal(R.raw.buyurtma_bekor_qilindi)
    }

    fun playSoundStartTaximeter() {
        playSoundInternal(R.raw.kettik)
    }

    // Call this method to release resources when the SoundManager is no longer needed
    fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}