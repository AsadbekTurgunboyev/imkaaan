package com.example.taxi.utils

import android.os.Handler
import android.os.Looper

class ForwardsTimerUtil(private val tickInterval: Long) {

    private var elapsedTime: Long = 0
    private var isRunning: Boolean = false
    private val handler = Handler(Looper.getMainLooper())
    private var callback: TimerCallback? = null

    fun start(startingSeconds: Long, callback: TimerCallback) {
        elapsedTime = startingSeconds * 1000 // Convert seconds to milliseconds
        this.callback = callback
        if (!isRunning) {
            isRunning = true
            handler.post(tickRunnable)
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(tickRunnable)
    }

    fun reset() {
        elapsedTime = 0
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                callback?.onTick(elapsedTime)
                elapsedTime += tickInterval
                handler.postDelayed(this, tickInterval)
            }
        }
    }

    interface TimerCallback {
        fun onTick(elapsedTime: Long)
    }
}
