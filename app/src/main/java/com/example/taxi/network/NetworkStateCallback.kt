package com.example.taxi.network

import android.net.ConnectivityManager
import android.net.Network

class NetworkStateCallback(private val onNetworkAvailable: () -> Unit, private val onNetworkLost: () -> Unit) : ConnectivityManager.NetworkCallback() {

    override fun onAvailable(network: Network) {
        // Network is available, trigger the callback
        onNetworkAvailable()
    }

    override fun onLost(network: Network) {
        // Network is lost, trigger the callback
        onNetworkLost()
    }
}