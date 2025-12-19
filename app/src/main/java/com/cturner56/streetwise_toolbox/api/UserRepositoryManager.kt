package com.cturner56.streetwise_toolbox.api

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class UserRepositoryManager {
    private val TAG = "CIT - UserRepositoryManager"
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    /**
     * A function which is responsible for retrieving the user preference for enabling, and or
     * disabling network refreshes for a logged in user.
     */
    suspend fun getNetworkRefreshPreference(): Boolean {
        val uid = userId ?: return false
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            doc.getBoolean("disableNetworkRefresh") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch user preference.", e)
            false
        }
    }

    /**
     * A function which is responsible for updating the
     * network refresh preference for a user to their Firestore instance.
     */
    suspend fun updateNetworkRefreshPreferences(isDisabled: Boolean) {
        val uid = userId ?: return
        try {
            val userDocRef = firestore.collection("users").document(uid)
            val preference = mapOf("disableNetworkRefresh" to isDisabled)
            userDocRef.set(preference, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user preference to Firestore.", e)
        }
    }

    /**
     * A function which is responsible for fetching user-saved repository data associated to their
     * account.
     */
    suspend fun getUserSavedRepoIds(): List<String> {
        val uid = userId ?: return emptyList()
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            @Suppress("UNCHECKED_CAST") // Safely cast from Firestore Object to List<String>
            doc.get("savedRepositories") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed load user submitted repos from Firestore", e)
            emptyList()
        }
    }

    /**
     * A function which is responsible for adding a user-submitted repository data associates it to
     * their account.
     */
    suspend fun addSavedRepo(repoFullName: String) {
        val uid = userId ?: return
        try {
            firestore.collection("users").document(uid)
                .update("savedRepositories", FieldValue.arrayUnion(repoFullName))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add user submitted repo to Firestore.", e)
        }
    }

    /**
     * A function which is responsible for deleted user-submitted repository data associated with
     * their account.
     */
    suspend fun removeSavedRepo(repoFullName: String) {
        val uid = userId ?: return
        try {
            firestore.collection("users").document(uid)
                .update("savedRepositories", FieldValue.arrayRemove(repoFullName))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove user-submitted repository from Firestore.", e)
        }
    }
}