package com.cturner56.cooperative_demo_2;

/*
* IUserService:
* API-Ref:
* https://github.com/RikkaApps/Shizuku-API/blob/a27f6e4151ba7b39965ca47edb2bf0aeed7102e5/demo/src/main/aidl/rikka/shizuku/demo/IUserService.aidl#L4
* Purpose:
* Allows for bridged communication between client-app and Shizuku's remote process.
*/

interface IUserService {
    void destroy ();
    void exit();
    // String getUName(); WIP -Still need to integrate separate UserService class file to execute command.
}