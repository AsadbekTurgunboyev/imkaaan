package com.example.taxi.socket

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.taxi.di.MAIN
import com.example.taxi.domain.model.socket.SocketMessage
import com.example.taxi.domain.preference.UserPreferenceManager
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI


class SocketRepository constructor(
    val context: Context,
    private var viewModelScope: CoroutineScope? = null,
    private val userPreferenceManager: UserPreferenceManager,
    private var socketMessageProcessor: SocketMessageProcessor? = null,
    private val onMessageReceived: () -> Unit
) {

    private var connectionListener: ConnectionListener? = null
    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected()
    }

    fun setOnConnectionListener(listener: ConnectionListener) {
        connectionListener = listener
    }


    private var webSocket: WebSocketClient? = null
    private var shouldReconnect = true

    private var _isConnected: Boolean = false

    var isConnected: Boolean
        get() = _isConnected
        set(value) {
            if (_isConnected != value) {
                _isConnected = value
            }
        }
    var reconnectJob: Job? = null

    private val handler = Handler(Looper.getMainLooper())


    fun initSocket(token: String) {
        if (reconnectJob?.isActive == true || isConnected) {
            return
        }

        reconnectJob = viewModelScope?.launch {
            connectSocket(token)
        }
    }

    private fun connectSocket(token: String) {
        shouldReconnect = true

        webSocket = object : WebSocketClient(URI("wss://$MAIN/connect/?token=$token")) {
            override fun onOpen(handshakedata: ServerHandshake?) {

                isConnected = true
                connectionListener?.onConnected()
            }

            override fun onMessage(message: String?) {
                val gson = Gson()
                val orderResponse = gson.fromJson(message, SocketMessage::class.java)
                if (orderResponse.key == "order_new" && userPreferenceManager.getDriverStatus() == UserPreferenceManager.DriverStatus.COMPLETED){
                    onMessageReceived()
                }
                message?.let {
                    socketMessageProcessor?.handleMessage(it)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                isConnected = false
                if (shouldReconnect) {
                    reconnectSocket(token)
                }

                connectionListener?.onDisconnected()
            }

            override fun onError(ex: Exception?) {
                isConnected = false


                if (shouldReconnect) {
                    reconnectSocket(token)
                }
                connectionListener?.onDisconnected()
            }
        }

        webSocket?.connect()
    }

    private fun reconnectSocket(token: String) {
        reconnectJob?.cancel()
        if (reconnectJob?.isActive != true || !isConnected) {
            reconnectJob = viewModelScope?.launch {
                delay(SocketConfig.RECONNECT_DELAY_MS)
                connectSocket(token)
            }
        }
    }


    fun disconnectSocket() {
        shouldReconnect = false
        userPreferenceManager.saveToggleState(false)

        handler.removeCallbacksAndMessages(null)
        reconnectJob?.cancel()
        webSocket?.close()

    }
}