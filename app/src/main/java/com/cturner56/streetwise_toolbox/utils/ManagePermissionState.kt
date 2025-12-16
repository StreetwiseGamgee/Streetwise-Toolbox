package com.cturner56.streetwise_toolbox.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * A composable function that acts as a state holder for Shizuku's permission status, and request logic.
 *
 * @param onServiceNotRunning A callback which is invoked if the Shizuku service is not running.
 * @param content A trailing lambda which receives both 'isGranted' and 'requestPermission'.
 */
@Composable
fun ManagePermissionState(
    onServiceNotRunning: () -> Unit,
    content: @Composable (isGranted: Boolean, requestPermission: () -> Unit) -> Unit) {

    /**
     * A boolean state which holds whether or not the permission is granted.
     * It persists across configuration changes via [rememberSaveable]
     */
    var isGranted by rememberSaveable { mutableStateOf(false) } // Remembers perm state.

    /**
     * An integer state used as a key to manually retrigger the [LaunchedEffect].
     * When the key increments, it forces a permission check to rerun.
     */
    var key by rememberSaveable { mutableStateOf(0) } // Allows key to trigger the effect again if required.

    LaunchedEffect(key) { // Effect runs on initial composition, and when the key changes.
        PermissionRequestHandler.requestHandler(
            onPermissionResult = { granted ->
                isGranted = granted
            },
            onServiceNotRunning = onServiceNotRunning
        )
    }

    /**
     * The lambda is passed down to the content,
     * and is used to re-initiate the permission check.
     */
    val requestPermission = {
        key++
        Unit // Must explicitly return Unit
    }
    content(isGranted, requestPermission) // Renders content
}