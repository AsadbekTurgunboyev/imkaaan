package com.example.taxi.socket

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.taxi.R
import com.example.taxi.components.service.KillStateDialogService
import com.example.taxi.domain.model.location.LocationRequest
import com.example.taxi.domain.model.socket.SocketMessage
import com.example.taxi.domain.model.socket.SocketOrderCancelledData
import com.example.taxi.ui.home.HomeActivity
import com.example.taxi.ui.home.order.OrderViewModel
import com.example.taxi.utils.JsonUtils
import com.example.taxi.utils.NotificationUtils
import com.squareup.moshi.Types
import org.json.JSONObject


class SocketMessageProcessor(
    private val context: Context,
    private val orderViewModel: OrderViewModel
) {


    fun sendLocation(request: LocationRequest) {
        orderViewModel.sendLocation(request)
    }

    fun handleMessage(message: String) {
        val type = Types.newParameterizedType(SocketMessage::class.java, Any::class.java)
        val socketMessage = JsonUtils.fromJson<SocketMessage<*>>(message, type)

        socketMessage?.let {
            handleSocketMessage(it, message)
        }
    }

    private fun handleSocketMessage(socketMessage: SocketMessage<*>, message: String) {
        if (socketMessage.status == 200) {
            when (socketMessage.key) {
                SocketConfig.ORDER_NEW -> handleNewOrder(message)
                SocketConfig.ORDER_UPDATE -> handleOrderUpdate(message)
                SocketConfig.ORDER_CANCELLED -> handleOrderCancelled(message)
                SocketConfig.SEND_NOTIFICATION_NEW_ORDER -> handleSendNotificationNewOrder(message)
                SocketConfig.SEND_NOTIFICATION -> handleSendNotification(message)
                SocketConfig.ORDER_ONLY_FOR_YOU -> startForegroundServiceWithMessage(message)
                SocketConfig.ORDER_ACCEPTED -> handleOrderAccepted(message)
                SocketConfig.PAYMENT_RECEIVED -> handlePaymentReceived()
            }
        }
    }

    private fun handlePaymentReceived(){
        val mediaPlayer = MediaPlayer.create(context, R.raw.payment_received)
        mediaPlayer.start()

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }

    private fun handleNewOrder(message: String) {
        sendBroadcast("OrderData_new", message)
        showNotificationNewOrder(message)
    }

    private fun handleOrderUpdate(message: String) {
        sendBroadcast("orderData_update", message)
    }

    private fun handleOrderCancelled(message: String) {
        val type = Types.newParameterizedType(
            SocketMessage::class.java,
            SocketOrderCancelledData::class.java
        )
        val orderCancelledData =
            JsonUtils.fromJson<SocketMessage<SocketOrderCancelledData>>(message, type)
        orderCancelledData?.let {
            val intentExtras = Bundle().apply {
                putInt("OrderData_canceled", it.data.order_id)
                putString(SocketConfig.ORDER_CANCELLED, SocketConfig.ORDER_CANCELLED)
                putInt("driver_id", it.data.driver_id ?: -1)
            }
            sendBroadcast(intentExtras)
        }
    }

    private fun handleSendNotificationNewOrder(message: String) {
        val jsonObject = JSONObject(message)
        val data = jsonObject.getJSONObject("data")
        val messageText = data.getString("message")
        showNotification(messageText, "Xabar")
    }


    private fun handleSendNotification(message: String) {
        val jsonObject = JSONObject(message)
        val data = jsonObject.getJSONObject("data").getJSONObject("data")
        val messageText = data.getString("message")

        showNotification(messageText, context.getString(R.string.messag))
    }

    private fun handleOrderAccepted(message: String) {
        val type = Types.newParameterizedType(
            SocketMessage::class.java,
            SocketOrderCancelledData::class.java
        )
        val orderAcceptedData =
            JsonUtils.fromJson<SocketMessage<SocketOrderCancelledData>>(message, type)
        orderAcceptedData?.let {
            val intentExtras = Bundle().apply {
                putInt("OrderData_canceled", it.data.order_id)
                putString(SocketConfig.ORDER_CANCELLED, SocketConfig.ORDER_ACCEPTED)
            }

            sendBroadcast(intentExtras)
        }
    }

    private fun sendBroadcast(action: String, message: String) {
        val intent = Intent("com.example.taxi.ORDER_DATA_ACTION").apply {
            putExtra(action, message)
        }
        context.sendBroadcast(intent)
    }

    private fun sendBroadcast(extras: Bundle) {
        val intent = Intent("com.example.taxi.ORDER_DATA_ACTION").apply {
            putExtras(extras)
        }
        context.sendBroadcast(intent)
    }

    private fun showNotificationNewOrder(message: String) {
        val result = JsonUtils.parseJsonAndExtractValues(message)
        val pendingIntent = NotificationUtils.createPendingIntent(
            context,
            HomeActivity::class.java,
            Bundle().apply {
                putBoolean("navigate_to_order_fragment", true)
            })

        NotificationUtils.showNotification(
            context = context,
            channelId = "channel_notif",
            title = context.getString(R.string.new_order),
            contentText = "${result.first}\n${context.getString(R.string.price)} \u2265${result.second}",
            pendingIntent = null,
            smallIcon = R.drawable.ic_bekjaanlogo
        )
    }

    private fun showNotification(contentText: String, title: String) {
        val pendingIntent = NotificationUtils.createPendingIntent(
            context,
            HomeActivity::class.java,
            Bundle().apply {
                putBoolean("navigate_to_order_fragment", true)
            })

        NotificationUtils.showNotification(
            context = context,
            channelId = "channel_notif",
            title = title,
            contentText = contentText,
            pendingIntent = pendingIntent,
            smallIcon = R.drawable.ic_message
        )
    }

    private fun startForegroundServiceWithMessage(message: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val intent = Intent(context, KillStateDialogService::class.java).apply {
                putExtra("message", message)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }


}