package com.example.taxi.domain.drive.currentDrive



class PauseCalculator {

    private var pausedTime: Long = 0
    private var lastPausedTime: Long = 0

    fun onPause() {
        if (lastPausedTime == 0L) {
            this.lastPausedTime = System.currentTimeMillis()
        }
    }

    fun considerPingForStopPause(pingTime: Long): Boolean {
        if (lastPausedTime > 0) {
            pausedTime += pingTime - lastPausedTime
            lastPausedTime = 0

            return true
        }

        return false
    }

    fun getPausedTime(pingTime: Long, p: Int): Long {
        if (lastPausedTime > 0) {
            return if (pingTime - lastPausedTime > 0){
                p + pausedTime + (pingTime - lastPausedTime)
            }else{
                p + pausedTime
            }
        }

        return pausedTime + p
    }
}