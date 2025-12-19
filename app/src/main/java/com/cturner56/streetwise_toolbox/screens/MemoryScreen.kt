package com.cturner56.streetwise_toolbox.screens

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cturner56.streetwise_toolbox.viewmodel.MemoryViewModel

/**
 * A composable function which is responsible for displaying the device's storage and
 * RAM information.
 *
 * It fetches the memory information by observing the state from the [MemoryViewModel]
 * Once the information is retrieved, it then displays the respective statistics in separate cards
 *
 * @param memoryViewModel The ViewModel instance which is responsible for providing up-to-date data.
 */
@Composable
fun MemoryScreen(memoryViewModel: MemoryViewModel = viewModel()) {

    val uiState by memoryViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
    Card (
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(25.dp)) {
            Text(
                text = "Memory Information",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            // https://developer.android.com/develop/ui/compose/animation/composables-modifiers
            val storageState = remember {
                MutableTransitionState(false).apply {
                    // Start the animation immediately.
                    targetState = true
                }
            }
            AnimatedVisibility(visibleState = storageState) {
                Column {
                    Row { Text("Total RAM: ${String.format("%.2f", uiState.totalRam)} MB's") }
                    Row { Text("Available RAM: ${String.format("%.2f", uiState.availableRam)} MB's") }
                    Row { Text( "Threshold RAM: ${String.format("%.2f", uiState.thresholdRam)} MB's") }
                }
            }
        }
    }
        Card (
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(25.dp))
            {
                Text(
                    text = "Storage Information",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                val storageState = remember {
                    // https://developer.android.com/develop/ui/compose/animation/composables-modifiers
                    MutableTransitionState(false).apply { targetState = true }
                }
                AnimatedVisibility(visibleState = storageState) {
                    Column {
                        Row { Text("Total Storage: " +
                                "${String.format("%.2f", uiState.totalStorage)} GB's")
                        }
                        Row { Text("Available Storage: " +
                                "${String.format("%.2f", uiState.availableStorage)} GB's")
                        }
                    }
                }
            }
        }
    }
}

