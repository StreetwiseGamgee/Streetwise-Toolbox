package com.cturner56.cooperative_demo_3.data

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.cturner56.cooperative_demo_3.IUserService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import rikka.shizuku.ShizukuSystemProperties
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Repository for executing shell commands via Shizuku UserService.
 * The object manages the connection to Shizuku's remote IUserService.
 * In addition the object provides methods to interact with such.
 *
 * Usage:
 *      ViewModel - Utilizes the object to fetch system APIs directly.
 * @property userService References the bound [IUserService] interface, otherwise such becomes null.
 * @property USER_SERVICE_COMPONENT_NAME The component name for the UserService to bind to.
 * @property deathRecipient A recipient which gets notified if the remote service is terminated.
 */
object ShellCmdletRepo {
    // Maintains a reference to the bound service.
    private var userService: IUserService? = null

    private val USER_SERVICE_COMPONENT_NAME = ComponentName(
        "com.cturner56.cooperative_demo_2",
        "com.cturner56.cooperative_demo_2.service.UserService"
    )

    private val deathRecipient = IBinder.DeathRecipient {
        Log.e("CIT - ShellCmdletRepo", "The UserService has died.")
        userService = null
    }

    /**
     * Binds to the remote UserService running on Shizuku.
     * The function will remain suspended until service is connected.
     *
     * @return True if the service was successfully bound.
     */
    private suspend fun bindUserService(): Boolean = suspendCoroutine { continuation ->
        if (userService != null) {
            Log.d("CIT - ShellCmdletRepo", "UserService is already bound.")
            continuation.resume(true)
            return@suspendCoroutine
        }

        val userServiceArgs = UserServiceArgs(USER_SERVICE_COMPONENT_NAME)
            .daemon(false)
            .processNameSuffix(":shizuku")
            .debuggable(false)
            .version(1)

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (service.isBinderAlive) {
                    Log.i("CIT - ShellCmdletRepo", "App has been bound to UserService")
                    userService = IUserService.Stub.asInterface(service)
                    try {
                        service.linkToDeath(deathRecipient, 0)
                    } catch (e: Exception) {
                        Log.e("CIT - ShellCmdletRepo", "Failed to linkToDeath", e)
                    }
                    continuation.resume(true)
                } else {
                    Log.e("CIT - ShellCmdletRepo", "Binder received but was dead.")
                    continuation.resume(false)
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                Log.e("CIT - ShellCmdletRepo", "The UserService has disconnected.")
                userService = null
            }
        }
        Shizuku.bindUserService(userServiceArgs, connection)
    }

    /**
     * Fetches the kernel's release version by executing a shell command via the UserService.
     * Prior to executing, it ensures it is connected to the aforementioned UserService.
     *
     * @return A string which contains the release version of the kernel, or a corresponding error.
     */
    suspend fun getUnameVersion(): String {
        return try {
            if (isPermissionDenied()) return "Shizuku permission is denied"

            if (userService == null) {
                if (!bindUserService()) {
                    return "Failed to bind to UserService"
                }
            }

            userService?.getUname() ?: "UserService is unavailable"
        } catch (e: Exception) {
            Log.e("CIT - ShellCmdletRepo", "Error getting uname version: ${e.message}", e)
            "Error fetching uname release version: ${e.message}"
        }
    }

    /**
     * Gets the kernel version from Android's system properties.
     * Reading from the read-only property 'ro.kernel.version'.
     *
     * @return A string containing the read-only property, or an error.
     */
    fun getKernelVersion(): String {
        return try {
            if (isPermissionDenied()) return "Permission has not been granted"

            val kernelVersion = ShizukuSystemProperties.get("ro.kernel.version", "")
            if (kernelVersion.isNotBlank()) {
                "Kernel version: $kernelVersion"
            } else {
                Log.w("CIT - ShellCmdletRepo", "ro.kernel.version property not found.")
                "Kernel version not found"
            }
        } catch (e: Exception) {
            Log.e("CIT - ShellCmdletRepo", "Error fetching kernel version: ${e.message}")
            "Error fetching kernel version: ${e.message}"
        }
    }

    /**
     * Checks if the applications has the required permission granted.
     *
     * @return True if the permission is denied, otherwise if it's granted it'll return true.
     */
    private fun isPermissionDenied(): Boolean {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.e("CIT - ShellCmdletRepo", "Permission has not been granted.")
            return true
        }
        return false
    }
}