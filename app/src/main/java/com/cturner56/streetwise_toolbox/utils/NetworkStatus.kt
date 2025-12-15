package com.cturner56.streetwise_toolbox.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * A utility object for network connectivity checking.
 */
object NetworkStatus {
    /**
     * The function [isOnline] uses [ConnectivityManager] and [NetworkCapabilities] in determining
     * whether or not a device has an active network connection.
     * docs-ref:
     * 1. "https://stackoverflow.com/questions/61138538/activenetworkinfo-and-isconnected-deprecated-in-kotlin#:~:text=context%20in%20comments.-,Comments,1991%207"
     * 2. https://stackoverflow.com/a/61691559 (Original code attribution)
     *
     * @param context The context which allows the application to access system services.
     * @return 'true' if an active connection is available, and 'false' if otherwise.
     */
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}