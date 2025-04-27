package com.example.taxi.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log


class NetworkMonitor(private val context: Context) : ConnectivityManager.NetworkCallback() {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun register() {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(this)
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        // Network reconnected
        Log.d("NetworkMonitor", "Network connected")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        // Network disconnected
        Log.d("NetworkMonitor", "Network disconnected")
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        // Check for changes in network speed
        val linkDownstreamBandwidthKbps = networkCapabilities.linkDownstreamBandwidthKbps
        val linkUpstreamBandwidthKbps = networkCapabilities.linkUpstreamBandwidthKbps
        Log.d("NetworkMonitor", "Downstream: $linkDownstreamBandwidthKbps kbps, Upstream: $linkUpstreamBandwidthKbps kbps")
    }
}
