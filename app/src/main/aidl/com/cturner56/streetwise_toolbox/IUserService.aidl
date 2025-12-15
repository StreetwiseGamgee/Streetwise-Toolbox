package com.cturner56.streetwise_toolbox;

/*
* IUserService:
* Android Developer Docs:
* https://developer.android.com/develop/background-work/services/aidl
* API-Ref:
* https://github.com/RikkaApps/Shizuku-API/blob/a27f6e4151ba7b39965ca47edb2bf0aeed7102e5/demo/src/main/aidl/rikka/shizuku/demo/IUserService.aidl#L4
* Purpose:
* Allows for bridged communication between client-app and Shizuku's remote process.
* It defines an interface which allows for "Inter-Process Communication" between the two apps.
*/

interface IUserService {
    void destroy () = 16777114; // Destroy method defined by Shizuku server
    void exit() = 1; // Exit method defined by user
    String getUname() = 2;
}