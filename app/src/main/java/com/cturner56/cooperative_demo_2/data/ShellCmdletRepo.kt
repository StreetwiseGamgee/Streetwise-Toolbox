package com.cturner56.cooperative_demo_2.data

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuSystemProperties

object ShellCmdletRepo {
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