package com.example.taxi

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.multidex.MultiDex
import com.example.taxi.custom.floatingwidget.FloatingWidgetView
import com.example.taxi.di.NetworkModule
import com.example.taxi.di.koinModule
import com.example.taxi.domain.drive.DriveService
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class TaxiApp : Application() {

    private var floatingWidgetView: FloatingWidgetView? = null
    private var isWidgetVisible = false
    private val driveService: DriveService by inject()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(AppLifecycleCallbacks())
        createNotificationChannels()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TaxiApp)
            modules(listOf(koinModule, NetworkModule))
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_notif",
                "Channel Notif",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "notification"


            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }


    inner class AppLifecycleCallbacks : ActivityLifecycleCallbacks {

        private var activityCount = 0

        override fun onActivityResumed(activity: Activity) {
            activityCount++
            if (activityCount > 0) {
                // Dastur oldingi rejimga qaytdi, vidjetni yashirish
                hideFloatingWidget()
            }
        }

        override fun onActivityPaused(activity: Activity) {
            activityCount--
            if (activityCount == 0) {
                // Dastur orqa fonga o'tdi, vidjetni ko'rsatish
                if(driveService.isRaceOngoing()){
                    showFloatingWidget(activity)
                }
            }
        }

        private fun showFloatingWidget(context: Context) {
            if (!isWidgetVisible) {
                // Vidjet faqat bir marta ko'rsatilsin
                floatingWidgetView = FloatingWidgetView(context)
                floatingWidgetView?.show()
                isWidgetVisible = true
            }
        }

        private fun hideFloatingWidget() {
            if (isWidgetVisible) {
                // Vidjetni yashirish
                floatingWidgetView?.hide()
                floatingWidgetView = null
                isWidgetVisible = false
            }
        }

        // Quyidagi metodlar ActivityLifecycleCallbacks interfeysining boshqa talablarini bajaradi
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }
}

