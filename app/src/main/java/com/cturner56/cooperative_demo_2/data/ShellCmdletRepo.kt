package com.cturner56.cooperative_demo_2.data

import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import com.cturner56.cooperative_demo_2.IUserService
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuSystemProperties
import rikka.shizuku.Shizuku.UserServiceArgs
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ShellCmdletRepo {
    // Maintains a reference to the bound service.
    private var userService: IUserService? = null

    private val USER_SERVICE_COMPONENT_NAME = ComponentName(
        "com.cturner56.cooperative_demo_2",
        "com.cturner56.cooperative_demo_2.service.UserService"
    )

    /**
     * Binds to the remote UserService running on Shizuku.
     * The function will remain suspended until service is connected.
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

        val connection = object : Shizuku.OnBinderReceivedListener {
            override fun onBinderReceived() {
                val binder = Shizuku.getBinder()
                if (binder != null && binder.isBinderAlive) {
                    Log.i("CIT - ShellCmdletRepo", "App has been bound to UserService")
                    userService = IUserService.Stub.asInterface(binder)
                    continuation.resume(true)
                } else {
                    Log.e("CIT - ShellCmdletRepo", "Binder received but was null / dead.")
                }
               Shizuku.removeBinderReceivedListener(this)
            }
        }
    }

    /**
     * Fetches kernel release version.
     * Executing 'uname -r' via the UserService
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
            "Error: ${e.message}"
        }
    }

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
            Log.e("CIT - ShellCmdletRepo", "Error getting kernel version: ${e.message}")
            "Error getting kernel version"
        }
    }

    private fun isPermissionDenied(): Boolean {
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Log.e("CIT - ShellCmdletRepo", "Permission has not been granted.")
            return true
        }
        return false
    }
}