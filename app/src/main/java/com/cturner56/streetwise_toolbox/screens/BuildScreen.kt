package com.cturner56.streetwise_toolbox.screens

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cturner56.streetwise_toolbox.utils.PermissionRequestHandler
import com.cturner56.streetwise_toolbox.viewmodel.BuildViewModel

/**
 * Retrieves a map of essential build.props from Android's Build class.
 * The properties provide details pertaining to the device's hardware and software.
 *
 * doc-ref: https://developer.android.com/reference/android/os/Build
 *
 * @return A map of key value pairs, IE: Manufacturer -> Google, Model -> Pixel 8
 */
fun getBuildProps(): Map<String, String> {
    return mapOf(
        "Brand" to Build.BRAND,
        "Manufacturer" to Build.MANUFACTURER,
        "Model" to Build.MODEL,
        "Codename" to Build.DEVICE,
        "Fingerprint" to Build.FINGERPRINT,
        "Version" to Build.VERSION.RELEASE
    )
}

/**
 * A function which is responsible for checking if Shizuku is installed.
 *
 * @param context The application context.
 */
fun isShizukuInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo("moe.shizuku.privileged.api", 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * Main composable which is responsible for displaying Android build.props and kernel information
 *
 * The screen observes the state from the [BuildViewModel] to show...
 * - Basic device build properties which fetched on initialization.
 * - A button to either download Shizuku, grant the permission, or redirect users to a video tutorial.
 * - If the remote service is setup, and the permission is granted it will display the kernel version.
 *
 * @param buildViewModel The ViewModel instance which is responsible for providing the data.
 */
@Composable
fun BuildScreen(
    buildViewModel: BuildViewModel = viewModel(),
) {
    // Collects state from the ViewModel.
    // Recomposition will occur whenever the state changes.
    val uiState by buildViewModel.uiState.collectAsState()

    val uriHandler = LocalUriHandler.current
    val shizukuDownloadUrl = "https://shizuku.rikka.app/download/"
    val shizukuSetupUrl = "https://drive.google.com/file/d/1bVa8xzHhbCA5jJLMqenYPXVcL5fBXhUU/view?usp=sharing"

    Column {
        Card(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(25.dp))
            {
                Text(
                    text = "Build Properties",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // https://developer.android.com/develop/ui/compose/animation/composables-modifiers
                val state = remember {
                    MutableTransitionState(false).apply {
                        // Start the animation immediately.
                        targetState = true
                    }
                }
                AnimatedVisibility(visibleState = state) {
                    Column {
                        uiState.buildProperties.forEach { (key, value) ->
                            DerivedProperty(key, value = value)
                        }
                    }
                }
            }
        }

        if (!uiState.isShizukuGranted) {
            Button(
                onClick = {
                    // If Shizuku is installed, request the permission.
                    if (uiState.isShizukuInstalled) {
                        PermissionRequestHandler.requestHandler(
                            onPermissionResult = { isGranted ->
                                if (isGranted) {
                                    buildViewModel.onShizukuPermissionGranted()
                                }
                            },
                            onServiceNotRunning = {
                                uriHandler.openUri(shizukuSetupUrl)
                            }
                        )

                    } else {
                        uriHandler.openUri(shizukuDownloadUrl) // Otherwise, open the download URL.
                    }
                },
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Text(text = if (uiState.isShizukuInstalled) "Request Kernel Information" else
                    "Install Shizuku to Request Kernel Information")
            }
        } else {
            Card( // Displays read-only kernel version fetched from Shizuku.
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                // https://developer.android.com/develop/ui/compose/animation/composables-modifiers
                val state = remember {
                    MutableTransitionState(false).apply {
                        // Start the animation immediately.
                        targetState = true
                    }
                }
                AnimatedVisibility(visibleState = state) {
                    Column(modifier = Modifier.padding(25.dp)) {
                        Text(
                            text = uiState.kernelVersion,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
            Card( // Displays Unix Name version fetched from Shizuku.
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                // https://developer.android.com/develop/ui/compose/animation/composables-modifiers
                val state = remember {
                    MutableTransitionState(false).apply {
                        // Start the animation immediately.
                        targetState = true
                    }
                }
                AnimatedVisibility(visibleState = state) {
                    Column(modifier = Modifier.padding(25.dp)) {
                        Text(
                            text = uiState.unameVersion,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A composable which is used to display build.props in a key-value pairing.
 *
 * @param key The property name text which is displayed on the left.
 * @param value The property value text which is displayed on the right.
 */
@Composable
fun DerivedProperty(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = key,
            modifier = Modifier.padding(end = 16.dp)
        )
        Text(
            text = value,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}