package com.cturner56.streetwise_toolbox.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cturner56.streetwise_toolbox.data.BuildData
import com.cturner56.streetwise_toolbox.data.ShellCmdletRepo
import com.cturner56.streetwise_toolbox.screens.getBuildProps
import com.cturner56.streetwise_toolbox.screens.isShizukuInstalled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * An [AndroidViewModel] which is responsible for facilitating the preparation and management of
 * data for the BuildScreen. The state of the UI is exposed through [uiState]
 * which is a [StateFlow] of [BuildData].
 *
 * @param application The application context required by the ViewModel to check if Shizuku is installed.
 */
class BuildViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(BuildData())
    val uiState: StateFlow<BuildData> = _uiState

    /**
     * Responsible for initializing the ViewModel by setting the initial state of the UI.
     * First, it fetches static build properties using [getBuildProps].
     * Then, it checks if Shizuku is installed using [isShizukuInstalled].
     */
    init {
        _uiState.update { currentState ->
            currentState.copy(
                buildProperties = getBuildProps(),
                isShizukuInstalled = isShizukuInstalled(application.applicationContext),
            )
        }
    }

    /**
     * A function which is responsible for handling the event when the user grants the permission.
     * It updates the UI state to indicate that the Shizuku permission has been granted.
     * Subsequently, it will attempt to fetch kernel information.
     */
    fun onShizukuPermissionGranted() {
        _uiState.update { it.copy(isShizukuGranted = true)}
        fetchKernelInfo()
    }

    /**
     * A function which is responsible for retrieving the kernel and uname versions asynchronously.
     * Utilizing [ShellCmdletRepo] it launches itself into the [viewModelScope] and performs operations
     * using [Dispatchers.IO] to prevent blocking the main thread.
     */
    private fun fetchKernelInfo() {
        viewModelScope.launch {
            val kernel = withContext(Dispatchers.IO) {
                ShellCmdletRepo.getKernelVersion()
            }
            val uname = withContext(Dispatchers.IO) {
                ShellCmdletRepo.getUnameVersion()
            }
            _uiState.update { it.copy(kernelVersion = kernel, unameVersion = uname)}
        }
    }
}