package com.cturner56.cooperative_demo_2.screens

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cturner56.cooperative_demo_2.ui.theme.CooperativeDemo1DeviceStatisticsTheme

private const val BYTES_PER_GIGABYTE = 1024.0 * 1024.0 * 1024.0

/**
 * A utility function which is responsible for converting bytes to gigabytes.
 *
 * @param bytes The number of bytes which will be converted.
 * @return The byte value provided into gigabytes as a [Double].
 */
fun convertBytesToGigabytes(bytes: Long): Double {
    return bytes / BYTES_PER_GIGABYTE
}

private const val BYTES_PER_MEGABYTE = 1024.0 * 1024.0
/**
 * A utility function which is responsible for converting bytes to megabytes.
 *
 * @param bytes The number of bytes which will be converted.
 * @return The byte value provided into megabytes as a [Double].
 */
fun convertBytesToMegabytes(bytes: Long): Double {
    return bytes / BYTES_PER_MEGABYTE
}

/**
 * A function which is responsible for retrieving the device's current memory statistics.
 *
 * It utilizes the [ActivityManager]'s system service to query the device for an
 * [ActivityManager.MemoryInfo] object which contains information about the device's RAM usage such as
 * total, available, and threshold memory.
 *
 * @param context The application context that is required to access the system service.
 * @return An [ActivityManager.MemoryInfo] object with respective memory categories populated.
 *
 * doc-ref: https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
 */
fun getAvailableMemory(context: Context): ActivityManager.MemoryInfo {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo
}

/**
 * A function which is responsible for calculating and formatting the device's internal storage consumption.
 *
 * It utilizes [StatFs] to retrieve the total, and available bytes from the device's '/data' directory.
 * The byte values are then converted into gigabytes, and represented inside a formatted string.
 *
 * @return A formatted [String] which displays the device's internal storage consumption.
 *      - Returning: Both the total storage, and storage available for use in Gigabytes.
 * doc-ref: https://developer.android.com/reference/android/os/StatFs
 */
@SuppressLint("DefaultLocale")
fun getAvailableStorage(): String {
    val path = Environment.getDataDirectory().path // Declares the internal storage path
    val stat = StatFs(path)

    val totalBytes = stat.totalBytes
    val availableBytes = stat.availableBytes

    val totalGigabytes: Double = convertBytesToGigabytes(totalBytes)
    val availableGigabytes: Double = convertBytesToGigabytes(availableBytes)

    return String.format(
        "Storage Usage: \n %.2f GB Total / %.2f GB Remaining",
        totalGigabytes,
        availableGigabytes
    )
}

/**
 * A composable function which is responsible for displaying the device's RAM information.
 *
 * It fetches the memory information once using [getAvailableMemory] and [remember].
 * Once the information is retrieved, it then displays the respective memory stats in a [Card].
 */
@SuppressLint("DefaultLocale")
@Composable
fun DisplayAvailableMemory() {
    val context = LocalContext.current
    val memoryInfo: ActivityManager.MemoryInfo = remember {
        getAvailableMemory(context)
    }
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
            Text("Total RAM: ${String.format("%.2f", convertBytesToMegabytes(memoryInfo.totalMem))} MB's")
            Text("Available RAM: ${String.format("%.2f", convertBytesToMegabytes(memoryInfo.availMem))} MB's")
            Text("Threshold RAM: ${String.format("%.2f", convertBytesToMegabytes(memoryInfo.threshold))} MB's")
        }
    }
}

/**
 * A composable function which is responsible for displaying the device's internal storage statistics.
 *
 * It is fetches the formatted string once using [getAvailableStorage] and [remember].
 * Once the information is retrieved, it then displays the respective storage stats in a [Card].
 */
@Composable
fun DisplayAvailableStorage() {
    val storageInfo: String = remember {
        getAvailableStorage()
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
            Text(storageInfo)
        }
    }
}

/**
 * A composable function which is responsible for displaying both the
 * [DisplayAvailableMemory] and [DisplayAvailableStorage] composables.
 */
@Composable
fun MemoryScreen() {
    Column (
        modifier = Modifier
            .padding(25.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp))
    {
        DisplayAvailableMemory()
        DisplayAvailableStorage()
    }
}

/**
 * A preview composable for the [MemoryScreen].
 * Providing a means to visualize the screen without running the application.
 */
@Preview(showBackground = true)
@Composable
fun MemoryScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        MemoryScreen()
    }
}

