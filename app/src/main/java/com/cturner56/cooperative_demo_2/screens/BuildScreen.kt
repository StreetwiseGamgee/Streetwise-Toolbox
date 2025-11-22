package com.cturner56.cooperative_demo_2.screens

import android.os.Build
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cturner56.cooperative_demo_2.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import androidx.compose.ui.unit.dp
import com.cturner56.cooperative_demo_2.data.ShellCmdletRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.withContext

fun getBuildProps(): Map<String, String> {
    return mapOf(
        // https://developer.android.com/reference/android/os/Build
        "Brand" to Build.BRAND,
        "Manufacturer" to Build.MANUFACTURER,
        "Model" to Build.MODEL,
        "Codename" to Build.DEVICE,
        "Fingerprint" to Build.FINGERPRINT,
        "Version" to Build.VERSION.RELEASE
    )
}

@Composable
fun BuildScreen(
    isShizukuGranted: Boolean,
    onRequestShizukuPermission: () -> Unit
) {
    val kernelVersion by produceState(initialValue = "Loading...", isShizukuGranted) {
        value = if (isShizukuGranted) {
            withContext(Dispatchers.IO) {
                ShellCmdletRepo.getKernelVersion()
            }
        } else {
            "Unable to fetch Kernel Version"
        }
    }

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
                val properties = getBuildProps()
                properties.forEach { (key, value) ->
                    DerivedProperty(key, value = value)
                }
            }
        }

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
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(25.dp)) {
                    Text(
                        text = kernelVersion,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
            }
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .fillMaxWidth()
            ) {
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

@Preview(showBackground = true)
@Composable
fun BuildPropsScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        BuildScreen(isShizukuGranted = false, onRequestShizukuPermission = {})
    }
}