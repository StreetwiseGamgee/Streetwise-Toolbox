package com.cturner56.streetwise_toolbox.utils

/**
 * A utility function which is responsible for comparing remote and local versions of application.
 * The function takes two version strings and returns an integer value indicating the comparison result.
 *
 * @param remoteReleaseVersion The remote version string from GitHub releases.
 * @param localAppVersion The local version string of the application.
 * @return An integer value indicating the comparison result:
 * - 1 if [remoteReleaseVersion] is greater than [localAppVersion].
 * - -1 if [remoteReleaseVersion] is less than [localAppVersion].
 * - 0 if [remoteReleaseVersion] is equal to [localAppVersion].
 */
fun compareVersionNames(remoteReleaseVersion: String, localAppVersion: String): Int {
    val remoteInstanceCleaned = remoteReleaseVersion.trim().removePrefix("v") // removes prefix defined by github release.
    val localInstanceCleaned = localAppVersion.trim()

    val remoteParts = remoteInstanceCleaned.split('.').map {it.toIntOrNull() ?: 0}
    val localParts = localInstanceCleaned.split('.').map {it.toIntOrNull() ?: 0}

    val maxParts = maxOf(remoteParts.size, localParts.size)

    for (i in 0 until maxParts) {
        val remoteParts = remoteParts.getOrElse(i) { 0 }
        val localParts = localParts.getOrElse(i) { 0 }

        if (remoteParts > localParts) return 1
        if (remoteParts < localParts) return -1
    }
    return 0
}