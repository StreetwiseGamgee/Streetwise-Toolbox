package com.cturner56.cooperative_demo_2.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object PermissionRequestHandler {

    // UID integer value which is used to identify our request.
    private const val REQUEST_CODE = 8812

    /**
     *  Requests Shizuku permission:
     *  It wait's for it's service to be available before executing,
     *  which inturn prevents runtime crashes
     *  @param onPermissionResult calls the result of the permission request
     *  (whether denied or granted)
     */
    fun requestHandler(onPermissionResult: (isGranted: Boolean) -> Unit) {
        // Checks if binder is already alive.
        if (Shizuku.isPreV11() || Shizuku.getBinder() != null) {
            // If the service is already active, check for permission.
            checkPermissionNow(onPermissionResult)
        } else {
            val binderListener = object : Shizuku.OnBinderReceivedListener {
                override fun onBinderReceived() {
                    checkPermissionNow(onPermissionResult)
                    Shizuku.removeBinderReceivedListener(this)
                }
            }
            Shizuku.addBinderReceivedListener(binderListener)
        }
    }

    private fun checkPermissionNow(onPermissionResult: (isGranted: Boolean) -> Unit) {
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            onPermissionResult(true)
            return
        }

        // Listener that waits for the results of the permission request from the Shizuku app.
        val permissionResultListener = object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if (requestCode == REQUEST_CODE) {
                    onPermissionResult(grantResult == PackageManager.PERMISSION_GRANTED)
                    Shizuku.removeRequestPermissionResultListener(this) // Listener cleanup
                }
            }
        }
        Shizuku.addRequestPermissionResultListener(permissionResultListener) // Registers the listener.
        Shizuku.requestPermission(REQUEST_CODE) // Triggers the request dialog.
    }
}