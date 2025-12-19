<div align = center>
  <h1> Streetwise's Toolbox</h1>
</div>

## Overview:
The main purpose of this application is to provide a streamlined means of fetching device information without needing root-level access.

## Architecture
The application follows **MVVM (Model-View-ViewModel)** architectural patterns to unsure separation of business logic and maintainability of the codebase.

<div align="center">
  <pre>
    UI (Composable Screens) 
    ↓ 
    ViewModels 
    ↓ 
    Repository 
    ↓ 
    Room DB  + Retrofit / Firestore
  </pre>
</div>

## Project Structure (High Level Overview)
<div>
  <pre>
com.cturner56.streetwise_toolbox/
│
├── api/                 # Network Services & Repository Managers
│
├── data/                # UI State Models
│
├── db/                  # Room Database & DAOs
│
├── screens/             # UI Composable Screens (Battery, Build, Memory...etc.)
│
├── viewmodel/           # ViewModels
│
├── navigation/          # Navigation Components (BottomBar, DropdownMenu)
│
├── MainActivity.kt      # App Entry Point
│
└── utils/               # Utility Helper Classes
  </pre>
</div>


## Tech Used:
### Shizuku - (Dev. [RikkaApps](https://github.com/RikkaApps/Shizuku))
- A companion application which provides a means to access hidden device information.
  - This is achieved by initiating a remote server process on the device.
    - By doing so, we are able to execute commands with elevated privileges which normally require ADB while connected to a computer, and or root-level permissions to access.
   
### Shizuku-API (Dev. [RikkaApps](https://github.com/RikkaApps/Shizuku-API))
- The API is used to safely request permissions and execute commands with elevated privileges provided by the Shizuku service.
  - Currently, this allows the device to retrieve kernel-specific information such as the ```ro.kernel.version``` and executes the shell command ```uname -r``` to retrieve the kernel's release version. 
- Licensed under the [MIT License](https://github.com/RikkaApps/Shizuku-API?tab=MIT-1-ov-file#readme), Copyright (c) 2021 RikkaW.

### Vico - (Dev. [patrykandpatrick](https://github.com/patrykandpatrick/vico))
- An immaculate charting library which powers Streetwise Toolbox to display live-graph readings for battery temperature fluctuations.
  - Fluctuations are graphed as the broadcasts containing the charging state, battery percentage, and temperature are detected to have changed.
- Licensed under the [Apache-2.0 License](https://github.com/patrykandpatrick/vico?tab=Apache-2.0-1-ov-file#readme), Copyright (c) 2024 Patryk Michalik

### Coil - (Dev. [coil-kt](https://github.com/coil-kt/coil))
- A library which is responsible for retrieving loading the user's profile picture in Streetwise Toolbox. 
- Licensed under the [Apache-2.0 License](https://github.com/coil-kt/coil?tab=Apache-2.0-1-ov-file#readme), Copyright (c) 2025 Coil Contributors

### Moshi - (Square)
- Provides **JSON deserialization** when fetching repository information from Github's [Rest API](https://docs.github.com/en/rest?apiVersion=2022-11-28)
  -  Used alongside Retrofit to convert the raw API responses into respective data classes.

### Room-DB - (Android Jetpack)
- Providing local persistence to data which is retrieved from GitHub's Restful API.

### Firebase Authentication - (Google)
- Allowing users to register and sign-in using Firebase Authentication.

### Firebase Firestore - (Google)
- Used to persist user preferences across login instances. Such as the network toggle state, in-app updater capability, and synchronizes user inserted repositories. 

## External Dependencies (Shizuku)
- [Shizuku Download](https://shizuku.rikka.app/download/) - Download App.
- [Shizuku User Manual](https://shizuku.rikka.app/guide/setup/) - Setup instructions.

### Features:
#### Battery Monitoring:
- The battery screen allows users to review their device's battery percentage, whether it's charging, and the internal temperature.
- Live-graph chart solution allows users to track battery temperature updates thanks to the wonderful library provided by Coil-kt. 

#### Build Properties / Kernel Info: 
- The build screen allows users to see device properties pertaining to their instance. It allows for quick retrieval of specific ```build.prop``` information.
- Additionally, users can see relevant kernel information if Shizuku is installed, the service running, and has granted ```Streetwise-Toolbox``` the requisite permission.
  - Having this information is particularly useful when deciding to root a device. Specifically by having the kernel release handy, we are then able to choose an appropriate root-solution which is correspondent to our version.
  - For instance say your phone's kernel release is ```6.1.24...``` we could then take such and navigate to [WildKernels](https://github.com/WildKernels/GKI_KernelSU_SUSFS?tab=readme-ov-file#-available-kernels) and choose an appropriate script to flash via a custom recovery.
  
#### Storage Information:
- Provides information pertaining to ram usage and storage consumption.

#### Repository Spotlight
- Provides a space where users can discover repositories pertaining to root-related modules.
  - Allowing users to navigate to both the respective GitHub repositories, and releases via hyperlinks.
    - Information is retrieved using GitHub's Restful API over the network, and subsequently stored locally.
    - (New!) **Cloud Sync & Hybrid Offline Mode**: Registered users can now insert and delete repositories from the spotlight screen.
    -  Information is stored using Firestore which allows users to have their custom entries sync across devices.
  - **Note**: Though if your a guest these entries will clear on exit as guest mode is intended to be a trial.
  
#### Account Authentication
- Providing a streamlined means of account registry, and subsequent logins.
  - Preventing the need for users to create a username, and or password.
- (New!) on the login screen I've implemented a video-background so users aren't blinded by the vacant void of whitespace.

### Proposed Features:
#### In-scope: 
##### Battery Health (Extended):
- ~~Provide a means to check the temperature of the device's battery~~ (Completed).
- ~~Implement a graph-chart visual to represent the fluctuations or differences in real-time~~ (Completed).

##### User Registry / Account Login:
- ~~Provide a means of account registry and authentication~~ (Completed).
- ~~Provide an in-app updater, or some type of notification system that let's users know when an update is available.~~

#### Out of Scope:
- Forum implementation: To allow users to share their discoveries, comment, and like other user submissions.
- CPU Temp monitoring: For the same reason as battery monitoring though I am uncertain whether I have time currently.
  
### Installation:
#### For developers:
```git clone https://github.com/StreetwiseGamgee/Cooperative-Demo-1.git```
1. Using android studio open the cloned project as an existing project.
2. Wait for respective build process to finish loading it's dependencies / configurations (Gradle Syncing).
3. Assemble the app and run the configuration to the target device.
    - By pressing ```shift + f10``` (On Windows) || ```Control + R``` (On Mac)
4. Wait for the install to finish, and enjoy!

5. ***An important note***, should you wish to make-use of the firebase/firestore features to test such in the IDE; you'll have to provide a google-services.json and setup a Firebase project of your own.
Requisite Directory Placement: ```app/google-services.json```


#### For users:
~~Will implement an apk for general use in the near future.~~ **SEE TAGGED RELEASES**

### How-to Contribute:
- Within the app there is a feedback section to which users can contact me.

## Contact Info:
- [cturner56@academic.rrc.ca](mailto:cturner56@academic.rrc.ca)
- [xblcit@gmail.com](mailto:xblcit@gmail.com)
