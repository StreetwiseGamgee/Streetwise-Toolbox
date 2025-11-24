package com.cturner56.cooperative_demo_2.destinations

open class Destination(val route:String) {
    object Battery: Destination("Battery")
    object Build: Destination("Build")
    object Memory: Destination("Memory")
    object About: Destination("About")
    object Feedback: Destination("Feedback")
    object RepoSpotlight: Destination("RepoSpotlight")
}