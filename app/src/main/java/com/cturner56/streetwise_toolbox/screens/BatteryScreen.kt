package com.cturner56.streetwise_toolbox.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cturner56.streetwise_toolbox.viewmodel.BatteryViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf

/**
 * A composable function which is responsible for displaying battery statistics.
 * The screen observes the state from the [BatteryViewModel] to show...
 *
 * - Battery percentage, charging status, and temperature. Additionally, providing live-graphing
 * of battery temperature fluctuations.
 *
 * -- NOTE --
 * For developers using a Device-Emulator:
 * The information displayed will be static as no updates are received after initial composition.
 * To test the complete functionality, it is best to do so using a physical device if possible.
 *
 * @param batteryViewModel The ViewModel instance which is responsible for providing up-to-date data.
 */
@Composable
fun BatteryScreen(
    batteryViewModel: BatteryViewModel = viewModel()
){
    val uiState by batteryViewModel.uiState.collectAsState()
    val chartModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(uiState.temperatureHistory) {
        val temperatureReadings = uiState.temperatureHistory.mapIndexed { index, temp ->
            entryOf(index, temp) }
        chartModelProducer.setEntries(temperatureReadings)
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
                    targetState = true // Start the animation immediately.
                }
            }
            AnimatedVisibility(visibleState = state) {
                Column{
                    Row {
                        Text("Hello, here's your current battery health!")
                    }
                    Row {
                        Text("Battery Percentage: ${uiState.percentage}%")
                    }
                    Row {
                        Text("Charging Status: ${if (uiState.isCharging) "Charging" 
                        else "Discharging"} ")
                    }
                    Row {
                        Text("Battery temperature: ${String.format("%.1f", uiState.temperature)}°C")
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
                        modifier = Modifier.height(250.dp)
                    )
                }
            }
        }
    }
}