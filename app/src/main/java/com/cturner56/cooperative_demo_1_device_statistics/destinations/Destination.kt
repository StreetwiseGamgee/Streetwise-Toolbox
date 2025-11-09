package com.cturner56.cooperative_demo_1_device_statistics.destinations

open class Destination(val route:String) {
    object Battery: Destination("Battery")
    object Build: Destination("Build")
    object Memory: Destination("Memory")
    object About: Destination("About")
    object Feedback: Destination("Feedback")
}