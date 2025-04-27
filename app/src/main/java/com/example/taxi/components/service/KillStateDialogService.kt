package com.example.taxi.components.service

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.taxi.R
import com.example.taxi.domain.model.order.Type
import com.example.taxi.domain.model.order.TypeEnum
import com.example.taxi.domain.model.socket.SocketMessage
import com.example.taxi.domain.model.socket.SocketOnlyForYouData
import com.example.taxi.domain.model.socket.toOrderData
import com.example.taxi.ui.home.HomeActivity
import com.example.taxi.ui.home.order.ServiceOrderAdapter
import com.example.taxi.utils.*
import com.example.taxi.utils.ConversionUtil.convertToKm
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

class KillStateDialogService : Service() {
    private var windowManager: WindowManager? = null
    private var dialogView: FrameLayout? = null
    private val handler = Handler(Looper.getMainLooper())
    private val notificationId = 1
    private var wakeLock: PowerManager.WakeLock? = null
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var soundPlayer: SoundPlayer
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val notificationChannelId = "KillStateDialogChannel"

    override fun onBind(intent: Intent?) = null


    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "myApp::MyWakelockTag"
        )
        ViewUtils.setLanguageForService(baseContext)


        checkOverlayPermission()
        setupDialogView()
        createNotificationChannel()
        soundPlayer = SoundPlayer(SoundPlayer.SoundType.LowSound, this)

    }

    override fun onDestroy() {
        super.onDestroy()
        dialogView?.let { windowManager?.removeView(it) }
        soundPlayer.stopSound()
        handler.removeCallbacksAndMessages(null)
    }

    private fun setupDialogView() {
        setTheme(R.style.AppTheme_Dialog)

        dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_only_for_you, null) as FrameLayout
        dialogView?.apply {
            background = ColorDrawable(Color.TRANSPARENT)
            layoutParams = createLayoutParams()
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(dialogView, dialogView?.layoutParams)
    }

    private fun createLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            dimAmount = 0.0f
            windowAnimations = android.R.style.Animation_Dialog
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            stopSelf()
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve the message from the intent extras
        val message = intent?.getStringExtra("message")

        if (message != null) {
            wakeLock?.acquire(10 * 60 * 1000L /*10 minutes*/)
            val type = Types.newParameterizedType(
                SocketMessage::class.java,
                SocketOnlyForYouData::class.java
            )
            val adapter: JsonAdapter<SocketMessage<SocketOnlyForYouData>> = moshi.adapter(type)
            try {
                val socketMessage = adapter.fromJson(message)
                if (socketMessage != null) {
                    val data = socketMessage.data as? SocketOnlyForYouData
                    data?.let { initVarDialog(it) }
                    dialogView?.visibility = View.VISIBLE
                }
                // Show the dialog view
            } catch (e: Exception) {
                Log.e("DialogService", "Error parsing JSON", e)
            }
            handler.postDelayed({
                dialogView?.visibility = View.GONE
                stopSelf()
                wakeLock?.release()
                soundPlayer.stopSound()

            }, 15000)

            if (allPermissionsGranted()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    startForeground(
                        notificationId,
                        createNotification(),
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                    )
                } else {
                    startForeground(notificationId, createNotification())
                }
            }else{
                Toast.makeText(this, "Ruxsat", Toast.LENGTH_SHORT).show()
            }

        } else {
            dialogView?.visibility = View.GONE
            soundPlayer.stopSound()
            stopSelf()
        }

        // Ensure the service is not terminated by the system
        return START_STICKY
    }

    private fun allPermissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 34) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.notification_channel_name)
            val channelDescription = "desc"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(notificationChannelId, channelName, importance).apply {
                    description = channelDescription
                }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    private fun initVarDialog(data: SocketOnlyForYouData) {
        setupViews(data)
        setupClickListeners(data)
        animateProgressBar()
        soundPlayer.playRequestSound()
    }

    fun updateTextView(type: Type, textView: AppCompatTextView) {
        val typeEnum = TypeEnum.values().find { it.ordinal + 1 == type.number }
        typeEnum?.let {
            textView.text = ConversionUtil.convertToCyrillic(type.name)
            textView.setTextColor(Color.parseColor(it.textColor))
            textView.compoundDrawableTintList = ColorStateList.valueOf(Color.parseColor(it.textColor))
            textView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it.backgroundColor))
        }
    }
    @SuppressLint("MissingPermission")
    private fun setupViews(data: SocketOnlyForYouData) {
        val priceTextView = dialogView?.findViewById<TextView>(R.id.priceTextView_dialog)
        val infoForPriceTv = dialogView?.findViewById<TextView>(R.id.infoTripTv)

        if (data.predict_cost != null && data.predict_distance != null) {
            infoForPriceTv?.text = getString(R.string.taxminiy_narx)
            priceTextView?.setPriceCost(data.predict_cost)

        } else {
            infoForPriceTv?.text = getString(R.string.boshlang_ich_summa)
            priceTextView?.setPriceCost(data.startCost)

        }

        val addressTextView = dialogView?.findViewById<TextView>(R.id.addressTextView_dialog)
        val typeTv = dialogView?.findViewById<AppCompatTextView>(R.id.textView_type_dialog)

        val t = Type(data.type.number,data.type.name)
        typeTv?.let { updateTextView(t, it) }
        val secondAddressTextView =
            dialogView?.findViewById<TextView>(R.id.secondDestinationAddress)
        val distanceTextView = dialogView?.findViewById<TextView>(R.id.distanceTextView_dialog)

        val service_recy = dialogView?.findViewById<RecyclerView>(R.id.service_recyclerView_dialog)
        val comment = dialogView?.findViewById<TextView>(R.id.comment_textView)

        service_recy?.adapter = ServiceOrderAdapter(data.toOrderData().services)
        data.type
        addressTextView?.convertToCyrillic(data.address.from)
        secondAddressTextView?.convertToCyrillic(data.address.to)
        comment?.text = data.comment?.ifEmpty {
            "-"
        }

        distanceTextView?.text = data.distance?.toDouble()
            ?.let { convertToKm(it) }
            ?.let { "%.2f".format(it) }
            ?.plus(" km")
    }


    private fun setupClickListeners(data: SocketOnlyForYouData) {
        val cancelOrderButton = dialogView?.findViewById<TextView>(R.id.cancelOrderButton)
        val acceptButton = dialogView?.findViewById<FrameLayout>(R.id.accept_button)

        cancelOrderButton?.setOnClickListener {
            dialogView?.visibility = View.GONE
            soundPlayer.stopSound()
            stopSelf()
        }

        acceptButton?.setOnClickListener {
            val intent = Intent(applicationContext, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("navigate_to_order", true)
            intent.putExtra("order_id", data.id)
            intent.putExtra("lat1", data.latitude1)
            intent.putExtra("lat2", data.latitude2)
            intent.putExtra("long1", data.longitude1)
            intent.putExtra("long2", data.longitude2)
            Log.d(
                "lokatsiya",
                "setupClickListeners: long2 = ${data.longitude2} , lat2 = ${data.latitude2}"
            )
            dialogView?.visibility = View.GONE
            soundPlayer.stopSound()
            startActivity(intent)
            stopSelf()
        }
    }



    private fun animateProgressBar() {
        val progressBar = dialogView?.findViewById<ProgressBar>(R.id.progress_bar)
        val objectAnimator = ObjectAnimator.ofInt(
            progressBar, "progress",
            progressBar!!.progress, 0
        ).setDuration(15000)

        objectAnimator.addUpdateListener { valueAnimator ->
            val progress = valueAnimator.animatedValue as Int
            progressBar.progress = progress
        }
        objectAnimator.start()
    }

    private fun createNotification(): Notification {
        val contentTitle = ""
        val contentText = ""
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, HomeActivity::class.java),
            flags
        )
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setSmallIcon(R.drawable.ic_admin)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)

        return notificationBuilder.build()
    }
}