package com.cturner56.streetwise_toolbox.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.cturner56.streetwise_toolbox.data.BatteryData

/**
 * A custom implementation of [BroadcastReceiver].
 * It's used to listen for changes in battery status via [Intent.ACTION_BATTERY_CHANGED].
 *
 * @property onBatteryUpdate A lambda function which accepts updated [BatteryData] values whenever the state changes.
 */
class BatteryUpdateReceiver(
    private val onBatteryUpdate: (BatteryData) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {

            // https://developer.android.com/training/monitoring-device-state/battery-monitoring#MonitorChargeState
            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val batteryPct: Int = (level * 100 / scale.toFloat()).toInt()

            // https://developer.android.com/training/monitoring-device-state/battery-monitoring#DetermineChargeState
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL

            // https://developer.android.com/reference/android/os/BatteryManager#EXTRA_TEMPERATURE
            // Inspiration:
            // https://www.reddit.com/r/Android/comments/13yjflc/android_14_adds_new_apis_that_apps_can_use_to/
            // https://stackoverflow.com/questions/18383581/how-get-battery-temperature-with-decimal
            val rawTemperature: Int = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val convertedTemperature = rawTemperature / 10.toFloat()

            val updatedBatteryData = BatteryData(
                percentage = batteryPct,
                isCharging = isCharging,
                temperature = convertedTemperature
            )
            onBatteryUpdate(updatedBatteryData)
        }
    }
}