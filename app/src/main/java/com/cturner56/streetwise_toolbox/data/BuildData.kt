package com.cturner56.streetwise_toolbox.data

/**
 * A data class which represents the state of the device's build properties and kernel information.
 *
 * @property buildProperties A map of key value pairs, IE: Manufacturer -> Google, Model -> Pixel 8
 * @property kernelVersion The current kernel version.
 * @property unameVersion The current uname version.
 * @property isShizukuGranted A boolean indicating whether the application has granted the permission.
 * @property isShizukuInstalled A boolean indicating whether the application has been installed.
 */
data class BuildData(
    val buildProperties: Map<String, String> = emptyMap(),
    val kernelVersion: String = "Loading...",
    val unameVersion: String = "Loading...",
    val isShizukuGranted: Boolean = false,
    val isShizukuInstalled: Boolean = false
)
