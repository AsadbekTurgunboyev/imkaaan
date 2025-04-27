package com.example.taxi.custom.timer
import kotlinx.coroutines.*

class CustomTimer(
    private val duration: Long,
    private val interval: Long,
    private val timerListener: TimerListener
) {
    private var coroutineScope: CoroutineScope? = null
    private var job: Job? = null
    private var remainingTime: Long = duration
    private var stopped: Boolean = false

    fun start() {
        coroutineScope?.cancel()
        coroutineScope = CoroutineScope(Dispatchers.Main)
        job = coroutineScope?.launch {
            for (time in remainingTime downTo 0 step interval) {
                if (stopped) return@launch
                remainingTime = time
                delay(interval)
                timerListener.onTick(remainingTime)
            }
            timerListener.onFinish()
        }
    }

    fun pause() {
        job?.cancel()
    }

    fun resume() {
        start()
    }

    fun stop() {
        job?.cancel()
        stopped = true
    }

    interface TimerListener {
        fun onTick(millisUntilFinished: Long)
        fun onFinish()
    }
}
