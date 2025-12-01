package com.cturner56.cooperative_demo_2.service;

import android.os.RemoteException;
import android.util.Log;
import com.cturner56.cooperative_demo_2.IUserService;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * A remote service which runs with Shizuku's elevated permissions to provide system information.
 *<p>
 *    The service is defined by the [IUserService.aidl] and is designed so that it'll be launched by
 *    Shizuku's service.
 *    ----
 *    It exposes methods which make use of shell commands to retrieve device information not normally privy
 *    to the user without direct root access or making use of a computer w/ adb access directly.
 *    ----
 *    doc-ref:
 *    <a href="https://github.com/RikkaApps/Shizuku-API/blob/a27f6e4151ba7b39965ca47edb2bf0aeed7102e5/demo/src/main/java/rikka/shizuku/demo/service/UserService.java#L12">...</a>
 *</p>
 */
public class UserService extends IUserService.Stub {
    private static final String USER_SERVICE = "UserService";

    /**
     * A method which is responsible for fetching the system's kernel release version.
     * <p>
     *     It makes use of the shell command 'uname -r' to print the release information.
     *     ----
     *     The output of the command is read, and subsequently returns the Unix Name (Kernel Release Ver.)
     *     If it's unable to return a valid uname, a subsequent message is logged, and returned.
     * </p>
     * @return The Unix Name release version. -r being the flag which indicates release.
     * @throws RemoteException if the process dies during the call.
     */
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
