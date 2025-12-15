package com.cturner56.streetwise_toolbox.data

/**
 * A data class which represents the state of the device's battery.
 */
data class BatteryData(
    val percentage: Int = 0,
    val isCharging: Boolean = false,
    val temperature: Float = 0.0f
)
