package com.cturner56.cooperative_demo_2.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cturner56.cooperative_demo_2.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import androidx.compose.ui.unit.dp
import com.cturner56.cooperative_demo_2.data.ShellCmdletRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
 * Main composable which is responsible for displaying Android build.props.
 * If the Shizuku permission is granted, it will retrieve and display additional info regarding the kernel.
 *
 * It utilizes [produceState] to load data asynchronously from [ShellCmdletRepo] when
 * the permission status changes.
 *
 * @param isShizukuGranted A bool which indicates whether the application has granted the permission.
 * @param onRequestShizukuPermission A lambda function which is invoked when a user clicks the grant
 * permission button.
 */
@Composable
fun BuildScreen(
    isShizukuGranted: Boolean,
    onRequestShizukuPermission: () -> Unit
) {
    // Fetches kernel version using Shizuku when the permission is granted.
    val kernelVersion by produceState(initialValue = "Loading...", isShizukuGranted) {
        value = if (isShizukuGranted) {
            withContext(Dispatchers.IO) {
                ShellCmdletRepo.getKernelVersion()
            }
        } else {
            "Unable to fetch Kernel Version"
        }
    }

    // Fetches the uname release version using Shizuku when the permission is granted.
    val unameVersion by produceState(initialValue = "Loading...", isShizukuGranted) {
        value = if (isShizukuGranted) {
            withContext(Dispatchers.IO) {
                ShellCmdletRepo.getUnameVersion()
            }
        } else {
            "Unable to fetch Unix Name"
        }
    }

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
                        val properties = getBuildProps()
                        properties.forEach { (key, value) ->
                            DerivedProperty(key, value = value)
                        }
                    }
                }
            }
        }

        // Conditionally displays permission request if Shizuku hasn't been granted.
        if (!isShizukuGranted) {
            Button(
                onClick = onRequestShizukuPermission,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Text(text = "Request Shizuku Permission")
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
                            text = kernelVersion,
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
                            text = unameVersion,
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key
        )
        Text(
            text = value
        )
    }
}

/**
 * A preview composable for the [BuildScreen].
 * Providing a means to visualize the screen without running the application.
 */
@Preview(showBackground = true)
@Composable
fun BuildScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        BuildScreen(isShizukuGranted = false, onRequestShizukuPermission = {})
    }
}