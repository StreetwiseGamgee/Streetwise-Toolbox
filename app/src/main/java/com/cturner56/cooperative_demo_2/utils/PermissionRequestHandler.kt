package com.cturner56.cooperative_demo_2.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

object PermissionRequestHandler {

    // UID integer value which is used to identify our request.
    private const val REQUEST_CODE = 8812

    /**
     *  A function which checks if the Shizuku permission is requested.
     *  If not, it presents a dialog-box to grant such IF!
     *  And when the necessary background service is running.
     *  @param onPermissionResult calls the result of the permission request (whether denied or granted)
     */

    fun requestHandler(onPermissionResult: (isGranted: Boolean) -> Unit) {
        // Should the permission already be granted, a callback will be triggered,
        // and subsequently exit the dialog quietly.
        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            onPermissionResult(true)
            return
        }

        // Listener that waits for the results of the permission request from the Shizuku app.
        val eventListener = object : Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantPermission: Int) {
                if (requestCode == REQUEST_CODE) {
                    onPermissionResult(grantPermission == PackageManager.PERMISSION_GRANTED)
                    Shizuku.removeRequestPermissionResultListener(this) // Listener cleanup
                }
            }
        }
        Shizuku.addRequestPermissionResultListener(eventListener) // Registers the listener.
        Shizuku.requestPermission(REQUEST_CODE) // Triggers the request dialog.
    }
}