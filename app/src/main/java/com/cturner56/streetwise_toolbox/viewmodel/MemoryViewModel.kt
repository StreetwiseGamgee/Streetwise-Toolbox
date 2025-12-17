package com.cturner56.streetwise_toolbox.viewmodel

import android.app.ActivityManager
import android.app.ActivityManager.MemoryInfo
import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import com.cturner56.streetwise_toolbox.data.MemoryData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

private const val BYTES_PER_GIGABYTE = 1024.0 * 1024.0 * 1024.0
private const val BYTES_PER_MEGABYTE = 1024.0 * 1024.0


class MemoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(MemoryData())
    val uiState = _uiState.asStateFlow()

    init {
        fetchMemoryAndStorageInfo()
    }

    /**
     * Responsible for fetching the latest memory and storage information.
     * Subsequently it updates the UI state with the new information.
     */
    fun fetchMemoryAndStorageInfo() {
        val memoryInfo = getAvailableMemory()
        val (totalStorage, availableStorage) = getStorageInformation()

        _uiState.update {
            it.copy(
                totalRam = convertBytesToMegabytes(memoryInfo.totalMem),
                availableRam = convertBytesToMegabytes(memoryInfo.availMem),
                thresholdRam = convertBytesToMegabytes(memoryInfo.threshold),
                totalStorage = totalStorage,
                availableStorage = availableStorage
            )
        }
    }

    /**
     * A private function which is responsible for retrieving the device's current memory statistics.
     *
     * doc-ref: https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
     */
    private fun getAvailableMemory(): MemoryInfo {
        val activityManager = getApplication<Application>().getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager
        return MemoryInfo().also { activityManager.getMemoryInfo(it) }
    }

    /**
     * A function which is responsible for retrieving the device's current storage statistics.
     *
     * It utilizes [StatFs] to fetch the total and available bytes from the device's
     * '/data' directory. The byte values are then converted into gigabytes using [convertBytesToGigabytes].
     *
     * @return A [Pair] of [Double] values representing the total and available storage in gigabytes.
     * doc-ref: https://developer.android.com/reference/android/os/StatFs
     */
    private fun getStorageInformation(): Pair<Double, Double> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val totalStorage = convertBytesToGigabytes(stat.totalBytes)
        val availableStorage = convertBytesToGigabytes(stat.availableBytes)
        return Pair(totalStorage, availableStorage)
    }

    /**
     * A private function which is responsible for converting bytes to gigabytes.
     */
    private fun convertBytesToGigabytes(bytes: Long): Double = bytes / BYTES_PER_GIGABYTE

    /**
     * A Private function which is responsible for converting bytes to megabytes.
     */
    private fun convertBytesToMegabytes(bytes: Long): Double = bytes / BYTES_PER_MEGABYTE

}