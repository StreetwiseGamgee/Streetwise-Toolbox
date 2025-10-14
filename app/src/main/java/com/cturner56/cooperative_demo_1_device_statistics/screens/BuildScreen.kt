package com.cturner56.cooperative_demo_1_device_statistics.screens

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cturner56.cooperative_demo_1_device_statistics.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import androidx.compose.ui.unit.dp

fun getBuildProps(): Map<String, String> {
    return mapOf(
        // https://developer.android.com/reference/android/os/Build
        "Brand" to Build.BRAND,
        "Manufacturer" to Build.MANUFACTURER,
        "Model" to Build.MODEL,
        "Codename" to Build.DEVICE,
        "Fingerprint" to Build.FINGERPRINT,
        // "Hardware" to Build.HARDWARE, Fetches same value as Codename
        "Version" to Build.VERSION.RELEASE
    )
}
@Composable
fun BuildScreen() {
    val properties = getBuildProps()
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
            properties.forEach { (key, value) ->
                DerivedProperty(key, value = value)
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
        BuildScreen()
    }
}