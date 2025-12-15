package com.cturner56.streetwise_toolbox.destinations

/**
 * Represents destinations within the app.
 *
 * @property route Represents the unique string identifier for each navigable route.
 */
open class Destination(val route:String) {
    object Battery: Destination("Battery")
    object Build: Destination("Build")
    object Memory: Destination("Memory")
    object About: Destination("About")
    object Feedback: Destination("Feedback")
    object RepoSpotlight: Destination("RepoSpotlight")
    object LoginScreen: Destination("Login")
}