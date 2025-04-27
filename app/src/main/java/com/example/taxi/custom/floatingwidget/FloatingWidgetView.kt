package com.example.taxi.custom.floatingwidget

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.taxi.R
import com.example.taxi.ui.home.HomeActivity

class FloatingWidgetView : ConstraintLayout, View.OnTouchListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val layoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START // Ekranning yuqori chap burchagiga joylashish
        x = 20
        y = 20
    }

    private var x: Int = 0
    private var y: Int = 0
    private var touchX: Float = 0f
    private var touchY: Float = 0f
    private var clickStartTimer: Long = 0
    private val windowManager: WindowManager
    private var isShown: Boolean = false // Widget ekranda mavjudligini kuzatish uchun flag

    init {
        View.inflate(context, R.layout.floating_widget_layout, this)
        setOnTouchListener(this)

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    companion object {
        private const val CLICK_DELTA = 200
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                clickStartTimer = System.currentTimeMillis()

                x = layoutParams.x
                y = layoutParams.y

                touchX = event.rawX
                touchY = event.rawY
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - clickStartTimer < CLICK_DELTA) {
                    val isAppInForeground = isAppInForeground()

                    if (!isAppInForeground) {
                        // Bring app to foreground
                        val intent = Intent(context, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } else {
                        windowManager.removeView(this)
                    }

                }
            }
            MotionEvent.ACTION_MOVE -> {
                layoutParams.x = (x + event.rawX - touchX).toInt()
                layoutParams.y = (y + event.rawY - touchY).toInt()
                windowManager.updateViewLayout(this, layoutParams)
            }
        }
        return true
    }

    private fun isAppInForeground(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return false

        for (processInfo in runningAppProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return processInfo.processName == context.packageName
            }
        }
        return false
    }

    // Widgetni ko'rsatish uchun
    fun show() {
        if (!isShown) { // Agar widget allaqachon ko'rsatilgan bo'lsa, qaytadan qo'shmang
            windowManager.addView(this, layoutParams)
            isShown = true
        }
    }

    // Widgetni yashirish uchun
    fun hide() {
        if (isShown) { // Faqat widget ko'rsatilgan bo'lsa, uni olib tashlang
            windowManager.removeView(this)
            isShown = false
        }
    }
}
