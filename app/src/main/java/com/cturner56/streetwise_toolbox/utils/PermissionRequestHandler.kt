package com.cturner56.streetwise_toolbox.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * A singleton object that provides the logic for requesting the Shizuku permission.
 *
 * The purpose of such is to prevent the app from crashing by ensuring the
 * Shizuku service is running prior to the permission request being sent.
 */
object PermissionRequestHandler {

    // UID integer value which is used to identify our request.
    private const val REQUEST_CODE = 8812

    /**
     *  A function which is responsible for safely beginning the permission check process.
     *  Acting as the main entry point for any screen which requires Shizuku's permission.
     *
     *  It waits for the service to be made available before passing [onPermissionResult]
     *  to the helper function [checkPermissionNow] which handles the permission logic further.
     *
     *  @param onPermissionResult A callback lambda which receives the final result from [checkPermissionNow]
     *      - Final result: 'true' if granted, and 'false' if not.
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

    /**
     * A function which is responsible for performing the direct permission check, and asking the
     * user for permission if such hasn't been granted already.
     *
     * The helper function assumes [requestHandler] has already been confirmed, and that the Shizuku
     * service is already active.
     *
     * @param onPermissionResult The callback that's invoked with the final permission result
     */
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