package com.example.taxi.utils

import android.content.Context
import com.example.taxi.R
import com.example.taxi.domain.preference.UserPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext

class TimerManager(
    val context: Context,
    var updateCallback: (Int, String) -> Unit
) {

    private var isCountingDown = true
    private var startTime: Long = 0
    private var minWaitTime: Int = 0
    private var moneyTime: Int = 0

    private var pauseTime: Long = 0
    private var transitionTime: Long = 0
    private val userPreferenceManager by lazy { GlobalContext.get().get<UserPreferenceManager>() }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var job: Job? = null


    init {
        loadFromPreferences()
        startTimer()
    }

    private fun loadFromPreferences() {
        with(userPreferenceManager) {
            startTime = getStartedTimeAcceptOrder()
            transitionTime = getTransitionTime()
            pauseTime = getPauseTime()
            isCountingDown = getIsCountingDown()
            minWaitTime = getMinWaitTime()
        }
    }

    private fun startTimer() {
        job = scope.launch {
            while (isActive) {
                updateTime()

                delay(1000)
            }
        }
    }

    private suspend fun updateTime() {
        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = ((currentTime - startTime) / 1000).toInt()
        val remainingTime = minWaitTime - elapsedSeconds

        withContext(Dispatchers.Main) {
            if (remainingTime >= 0) {
                updateCallback(remainingTime, context.getString(R.string.bepul_kutish))
            } else {
                val moneyTime = (elapsedSeconds - minWaitTime)
                updateCallback(moneyTime, context.getString(R.string.wait_money))
            }
        }
    }


    fun saveTransitionTime() {
        if (isCountingDown) {
            val currentTime = System.currentTimeMillis()
            transitionTime = currentTime
            moneyTime = 0
            userPreferenceManager.saveTransitionTime(currentTime)
            userPreferenceManager.saveIsCountingDown(false)
        } else {
            moneyTime = ((transitionTime - startTime) / 1000).toInt()
        }
        job?.cancel()
    }


    companion object Factory {

        const val TAG = "vaqtlar"
    }

    fun stop() {
        // Implement cleanup logic here, such as canceling the coroutine job
        job?.cancel()
    }

}
