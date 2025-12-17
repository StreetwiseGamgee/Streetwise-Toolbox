package com.cturner56.streetwise_toolbox.data

/**
 * A data class which represents the state of the device's memory.
 *
 * @property totalRam The total RAM available on the device in Megabytes.
 * @property availableRam The amount of RAM available on the device in Megabytes.
 * @property thresholdRam The threshold RAM available on the device in Megabytes.
 * @property totalStorage The total amount of storage on the device in Gigabytes.
 * @property availableStorage The amount of storage available on the device in Gigabytes.
 *
 * doc-ref: https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
 */
data class MemoryData (
    val totalRam: Double = 0.0,
    val availableRam: Double = 0.0,
    val thresholdRam: Double = 0.0,
    val totalStorage: Double = 0.0,
    val availableStorage: Double = 0.0
)