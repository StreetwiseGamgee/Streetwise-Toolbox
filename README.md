<div align = center>
  <h1> Streetwise's Toolbox</h1>
</div>

## Overview:
The main purpose of this application is to provide a streamlined means of fetching device information without needing root-level access.

## Tech Used:
### Shizuku - (Dev. [RikkaApps](https://github.com/RikkaApps))
- A companion application which provides a means to access hidden device information.
  - This is achieved by initiating a remote server process on the device.
    - By doing so, we are able to execute commands with elevated privledges which normally require ADB, and or root to access.

### Moshi - (Square)
- Provides **JSON deserialization** when fetching repository information from Github's [Rest API](https://docs.github.com/en/rest?apiVersion=2022-11-28)
  -  Used alongside Retrofit to convert the raw API responses into respective data classes.

### Room-DB - (Android Jetpack) - (WIP)
- Providing local persistence to data which is retrieved from external sources.
  - While I have the dependencies installed, I still need to implement such. 

### Firebase - (Google) - (WIP)
- Allowing users to register and sign-in using Firebase Authentication.
  - I don't currently have dependencies installed, and additional research is required to implement.

## Contact Info:
- [cturner56@academic.rrc.ca](mailto:cturner56@academic.rrc.ca)
- [xblcit@gmail.com](mailto:xblcit@gmail.com)

## Dependencies
- [Shizuku Download](https://shizuku.rikka.app/download/) - To see additional device information.
- [Shizuku User Manual](https://shizuku.rikka.app/guide/setup/) - Setup instructions.

### Features:
#### Battery Monitoring (WIP):
- Currently I have basic information such as the battery's percentage, and whether it's charging or not.
#### Build Properties / Kernel Info (WIP): 
- I have basic build.prop information being displayed.
- Relevant kernel information is displayed if a user has installed Shizuku, the service is running, and has granted the permission.
#### Storage Information:
- Provides information pertaining to ram usage and storage consumption.

### Proposed Features:
#### In-scope: 
##### Battery Health (Extended):
- Provide a means to check the temperature of the devices battery.
- Implement a graph-chart visual to represent the fluctuations or differences in real-time.

##### User Registry / Account Login:
- Provide a means of account registry and authentication.
- Provide an in-app updater, or some type of notification system that let's users know when an update is available.

#### Out of Scope:
- Forum implementation: To allow users to share their discoveries, comment, and like other user submissions.
- CPU Temp monitoring: For the same reason as battery monitoring though I am uncertain whether I have time currently.
  
### Installation:
#### For developers:
```git clone https://github.com/StreetwiseGamgee/Cooperative-Demo-1.git```
#### For users:
Will implement an apk for general use in the near future.

### How-to Contribute:
- Within the app there is a feedback section to which users can contact me.

