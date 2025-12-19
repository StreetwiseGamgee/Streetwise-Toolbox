package com.cturner56.streetwise_toolbox.viewmodel

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.AndroidViewModel
import com.cturner56.streetwise_toolbox.data.BatteryData
import com.cturner56.streetwise_toolbox.recievers.BatteryUpdateReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

const val MAX_TEMPERATURE_ENTRIES = 20

/**
 * A [AndroidViewModel] which is responsible for facilitating the preparation and management of
 * data for the BatteryScreen.
 *
 * The ViewModel handles the lifecycle of the [BatteryUpdateReceiver] to ensure it's only registered
 * when the ViewModel is active. Otherwise, it is subsequently unregistered.
 */
class BatteryViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BatteryData())
    val uiState = _uiState.asStateFlow()
    private var temperatureHistory = listOf<Float>()

    private val batteryUpdateReceiver = BatteryUpdateReceiver { batteryData ->
        temperatureHistory =
            ( temperatureHistory + batteryData.temperature).takeLast(MAX_TEMPERATURE_ENTRIES)

        _uiState.update {
            it.copy(
                percentage = batteryData.percentage,
                isCharging = batteryData.isCharging,
                temperature = batteryData.temperature,
                temperatureHistory = temperatureHistory
            )
        }
    }

    /**
     * Initializes the ViewModel by registering a [BatteryUpdateReceiver]
     * to listen for battery changes.
     */
    init {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        application.registerReceiver(batteryUpdateReceiver, filter)
    }

    /**
     * Responsible for cleaning up the ViewModel by unregistering the [BatteryUpdateReceiver].
     * This is for security purposes to prevent memory leaks.
     */
    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(batteryUpdateReceiver)
    }
}