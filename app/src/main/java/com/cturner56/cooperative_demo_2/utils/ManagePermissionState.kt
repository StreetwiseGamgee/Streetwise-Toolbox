package com.cturner56.cooperative_demo_2.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun ManagePermissionState(
    content: @Composable (isGranted: Boolean, requestPermission: () -> Unit) -> Unit) {

    var isGranted by rememberSaveable { mutableStateOf(false) } // Remembers perm state.
    var key by rememberSaveable { mutableStateOf(0) } // Allows key to trigger the effect again if required.

    LaunchedEffect(key) {
        PermissionRequestHandler.requestHandler{ granted ->
            isGranted = granted
        }
    }

    val requestPermission = {
        key++
        Unit // Must explicitly return Unit
    }
    content(isGranted, requestPermission)
}