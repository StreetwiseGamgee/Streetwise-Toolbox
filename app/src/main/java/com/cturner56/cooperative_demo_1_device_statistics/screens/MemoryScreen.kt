package com.cturner56.cooperative_demo_1_device_statistics.screens

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
import com.cturner56.cooperative_demo_1_device_statistics.ui.theme.CooperativeDemo1DeviceStatisticsTheme

private const val BYTES_PER_GIGABYTE = 1024.0 * 1024.0 * 1024.0
fun convertBytesToGigabytes(bytes: Long): Double {
    return bytes / BYTES_PER_GIGABYTE
}

private const val BYTES_PER_MEGABYTE = 1024.0 * 1024.0
fun convertBytesToMegabytes(bytes: Long): Double {
    return bytes / BYTES_PER_MEGABYTE
}

// https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
fun getAvailableMemory(context: Context): ActivityManager.MemoryInfo {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memoryInfo = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memoryInfo)
    return memoryInfo
}

// https://developer.android.com/reference/android/os/StatFs
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
@SuppressLint("DefaultLocale")
@Composable
fun DisplayAvailableMemory() {
    val context = LocalContext.current
    val memoryInfo: ActivityManager.MemoryInfo = remember {
        getAvailableMemory(context)
    }
    Column {

        Text("Total RAM: ${String.format("%.2f", convertBytesToMegabytes(memoryInfo.totalMem))} MB's")
        Text("Available RAM: ${String.format("%.2f", convertBytesToMegabytes(memoryInfo.availMem))} MB's")
        Text("Threshold RAM: ${String.format("%.2f", convertBytesToMegabytes(memoryInfo.threshold))} MB's")
    }
}

@Composable
fun DisplayAvailableStorage() {
    val storageInfo: String = remember {
        getAvailableStorage()
    }
    Column {
        Text(storageInfo)
    }
}

@Composable
fun MemoryScreen() {
    Card(
        modifier = Modifier
        .padding(12.dp)
        .fillMaxWidth()
    ) {
        Column (
            modifier = Modifier
                .padding(25.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp))
        {
            Text(
                text = "Storage & Memory Information",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row {
                DisplayAvailableMemory()
            }
            Row {
                DisplayAvailableStorage()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MemoryScreenPreview() {
    CooperativeDemo1DeviceStatisticsTheme {
        MemoryScreen()
    }
}

