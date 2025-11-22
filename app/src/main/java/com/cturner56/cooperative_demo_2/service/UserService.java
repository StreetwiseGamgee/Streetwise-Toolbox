package com.cturner56.cooperative_demo_2.service;

import android.os.RemoteException;
import android.util.Log;
import com.cturner56.cooperative_demo_2.IUserService;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
 * UserService:
 * API-Ref:
 * https://github.com/RikkaApps/Shizuku-API/blob/a27f6e4151ba7b39965ca47edb2bf0aeed7102e5/demo/src/main/java/rikka/shizuku/demo/service/UserService.java#L12
 * Purpose: A service which runs with Shizuku's permissions to expose
 * system information by executing shell commands.
 */
public class UserService extends IUserService.Stub {
    private static final String USER_SERVICE = "UserService";

    // Method which retrieves system's kernel information by executing the command 'uname -r'
    public String getUname() throws RemoteException {
        try {
            // Initiates new process to run the command.
            Process process = new ProcessBuilder("uname", "-r").start();

            // Read the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String kernelVersion = reader.readLine();

            // Wait for process to exit, and logs such.
            int exitCode = process.waitFor();
            Log.i(USER_SERVICE, "uname -r has exited with " + exitCode);

            // returns the kernel version
            return (kernelVersion != null && !kernelVersion.isEmpty()) ? kernelVersion: "Kernel release version not found";
        } catch (Exception e) {
            Log.e(USER_SERVICE, "Failed to execute command 'uname -r'");
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Reserved Destroy Method
     * The method is only called when the service is no longer required by the system.
     */
    @Override
    public void destroy() {
        Log.i("UserService", "destroy");
        System.exit(0);
    }
    @Override
    public void exit() {
        destroy();
    }
}
