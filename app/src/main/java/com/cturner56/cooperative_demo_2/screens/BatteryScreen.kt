package com.cturner56.cooperative_demo_2.screens

import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cturner56.cooperative_demo_2.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import com.cturner56.cooperative_demo_2.recievers.BatteryReceiver

/**
 * A composable which retrieves, and displays the devices current battery info.
 * It displays battery information such as the percentage, and whether the device is charging.
 *
 * The function utilizes a sticky broadcast Intent...
 * [Intent.ACTION_BATTERY_CHANGED] to attain the battery percentage and charging status.
 */
@Composable
fun BatteryScreen(){
    val context = LocalContext.current
    var batteryTemperature by remember { mutableStateOf(0.0f) }
    DisposableEffect(context) {
        val batteryReceiver = BatteryReceiver { temp ->
            batteryTemperature = temp
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

    // https://developer.android.com/training/monitoring-device-state/battery-monitoring#MonitorChargeState
    val batteryPct: Float? = batteryStatus?.let { intent ->
        val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        level * 100 / scale.toFloat()
    }

    // https://developer.android.com/training/monitoring-device-state/battery-monitoring#DetermineChargeState
    val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
    val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
            || status == BatteryManager.BATTERY_STATUS_FULL

    Card(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Battery Information",
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
                Column{
                    Row {
                        Text("Hello, here's your current battery health!")
                    }
                    Row {
                        Text("Battery Percentage: ${batteryPct?.toInt() ?:
                        "Cannot fetch battery percentage"}")
                    }
                    Row {
                        Text("Charging Status: ${if (isCharging) "Charging" else "Discharging"} ")
                    }
                    Row {
                        Text("Battery temperature: ${batteryTemperature}°C")
                    }
                }
            }
        }
    }
}

/**
 * A preview composable for the [BatteryScreen].
 * Providing a means to visualize the screen without running the application.
 */
@Preview(showBackground = true)
@Composable
fun BatteryHealthScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        BatteryScreen()
    }
}