package com.cturner56.streetwise_toolbox.screens

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cturner56.streetwise_toolbox.data.BatteryData
import com.cturner56.streetwise_toolbox.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import com.cturner56.streetwise_toolbox.recievers.BatteryUpdateReceiver
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

const val MAX_TEMPERATURE_ENTRIES = 20

/**
 * A composable function which is responsible for displaying battery statistics.
 * It registers a [BatteryUpdateReceiver] to display battery info dynamically.
 * The screen displays the battery's percentage, charging status, and temperature.
 *
 * Additionally, it maintains a history of recent temperature readings and
 * displays them in a live-graph. The graph is created using Vico charting library
 * which allows for the visualization of fluctuations in temperature over a period of time.
 *
 * Making use of [DisposableEffect] to manage the lifecycle of the [BatteryUpdateReceiver] ensures
 * the reciever is only registered when the composable is active. Otherwise, it's unregistered and
 * disposed of. The information updates in real time as the
 * system broadcast ([Intent.ACTION_BATTERY_CHANGED]) is received.
 */
@Composable
fun BatteryScreen(){
    val context = LocalContext.current
    var temperatureHistory by remember { mutableStateOf(listOf<Float>()) }
    var batteryData by remember { mutableStateOf(BatteryData()) }

    val chartModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(temperatureHistory) {
        val temperatureReadings = temperatureHistory.mapIndexed { index, temp -> entryOf(index, temp) }
        chartModelProducer.setEntries(temperatureReadings)
    }

    DisposableEffect(context) {
        val batteryReceiver = BatteryUpdateReceiver { data ->
            batteryData = data
            // Update temperature history list.
            temperatureHistory = (temperatureHistory + data.temperature).takeLast(MAX_TEMPERATURE_ENTRIES)
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)
        onDispose {
            context.unregisterReceiver(batteryReceiver)
        }
    }

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
                        Text("Battery Percentage: ${batteryData.percentage}%")
                    }
                    Row {
                        Text("Charging Status: ${if (batteryData.isCharging) "Charging" 
                        else "Discharging"} ")
                    }
                    Row {
                        Text("Battery temperature: ${String.format("%.1f", batteryData.temperature)}°C")
                    }

                    Text(
                        text = "Battery Temperature Fluctuations (°C)",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)
                    )
                    Chart(
                        chart = lineChart(),
                        chartModelProducer = chartModelProducer,
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                        modifier = Modifier.height(200.dp)
                    )
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