package com.cturner56.cooperative_demo_2.recievers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager

/**
 *
 */
class BatteryReceiver(
    private val onTemperatureUpdate: (Float) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
            // https://developer.android.com/reference/android/os/BatteryManager#EXTRA_TEMPERATURE
            // Inspiration:
            // https://www.reddit.com/r/Android/comments/13yjflc/android_14_adds_new_apis_that_apps_can_use_to/
            // https://stackoverflow.com/questions/18383581/how-get-battery-temperature-with-decimal
            val rawTemperature: Int = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val convertedTemperature = rawTemperature / 10.toFloat()

            onTemperatureUpdate(convertedTemperature)
        }
    }
}