package com.example.taxi.dbModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {
    var pauseStartTime: Long = 0
    var time = 0
    var totalPauseTime: Long = 0
    var isPaused: Boolean = false
    private var job: Job? = null
    fun togglePauseResume(startTime: Int, updateUI: (Int) -> Unit) {

        time = startTime
        // Pause - save the pause start time
        pauseStartTime = System.currentTimeMillis()

        // Start the coroutine to update the UI
        job = viewModelScope.launch {
            while (isActive) {
                // Calculate current pause duration

                time++
                // Update the UI
                updateUI(time)

                // Delay for 1 second
                delay(1000)
            }
        }
    }

    // Toggle pause state

    fun stopTime() {
        job?.cancel()
    }
}

