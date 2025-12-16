package com.cturner56.streetwise_toolbox.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.cturner56.streetwise_toolbox.R
import com.cturner56.streetwise_toolbox.api.Api
import com.cturner56.streetwise_toolbox.viewmodel.AuthViewModel
import com.cturner56.streetwise_toolbox.BuildConfig
import java.util.concurrent.TimeUnit

class AppUpdateScheduler(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    private val TAG = "CIT - AppUpdateWorker"

    /**
     *
     */
    override suspend fun doWork(): Result {
        if (!AuthViewModel.getUpdatePreferenceBlocking()) {
            Log.i(TAG, "User not subscribed to updates, cancelling work future checks.")
            cancelWork(appContext)
            return Result.success()
        }
        Log.i(TAG, "User subscribed to updates, checking for updates.")
        return try {
            val latestRelease = Api.retrofitService.getReleases(
                owner = "StreetwiseGamgee",
                repo = "Streetwise-Toolbox"
            ).firstOrNull()

            val remoteVersion = latestRelease?.tagName
            val localVersion = BuildConfig.VERSION_NAME

            if (remoteVersion != null) {
                val comparisonResult = compareVersionNames(remoteVersion, localVersion)
                if (comparisonResult > 0) {
                    Log.i(TAG, "New version available: $remoteVersion")
                    showUpdateNotification(appContext, remoteVersion)
                } else {
                    Log.i(TAG, "No new version available.")
                }
            } else {
                Log.w(TAG, "No releases found.")
            }
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking for updates: ${e.message}", e)
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(appContext, "Error checking for updates: ${e.message}", Toast.LENGTH_LONG).show()
            }
            Result.retry()
        }
    }

    /**
     *
     */
    private fun showUpdateNotification(context: Context, remoteVersion: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "APP_UPDATE_CHANNEL"

        // Create notification channel for Android Oreo and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Update Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for app updates"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/StreetwiseGamgee/Streetwise-Toolbox/releases"))
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_update_avail)
            .setContentTitle("New Version Available")
            .setContentText("A new version of the app is available: $remoteVersion")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(1, notification)
    }

    companion object {
        private const val WORKER_TAG = "AppUpdateWorker"

        /**
         *
         */
        fun scheduleWork(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<AppUpdateScheduler>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
            Log.i(WORKER_TAG, "Work request scheduled.")
        }

        /**
         *
         */
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelAllWorkByTag(WORKER_TAG)
            Log.i(WORKER_TAG, "Work request cancelled.")
        }
    }
}