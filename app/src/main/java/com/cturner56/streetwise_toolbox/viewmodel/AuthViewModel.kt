package com.cturner56.streetwise_toolbox.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cturner56.streetwise_toolbox.utils.AppUpdateScheduler
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CountDownLatch

/**
 * A sealed class which is responsible for the states of the user sign-in process.
 * Ensuring the UI can only be in a single state at a given time.
 */
sealed class SignInState {
    object Idle : SignInState()
    object Loading : SignInState()
    data class Success(val authResult: AuthResult) : SignInState()
    data class Error(val message: String) : SignInState()
    object GuestSuccess : SignInState()
}

/**
 * An [AndroidViewModel] which is responsible for handling user authentication logic.
 * The model exposes the [signInState] using [asStateFlow]. Using [viewModelScope] it launches
 * coroutines that are canceled when the ViewModel clears.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CIT - AuthViewModel" // Added re-usable TAG for simplified logging.
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * State-flows for UI State.
     */
    // A private prop which is responsible for holding the current state of authentication.
    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated = _isAuthenticated.asStateFlow()
    // A private prop which is responsible for holding the current state of notification preference.
    private val _updateNotificationPreference = MutableStateFlow(false)
    val updateNotificationPreference = _updateNotificationPreference.asStateFlow()
    // A private prop which is responsible for holding the current photo URL of the user.
    private val _photoUrl = MutableStateFlow(auth.currentUser?.photoUrl)
    val photoUrl = _photoUrl.asStateFlow()
    // A private prop which is responsible for holding the current state of guest mode.
    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode = _isGuestMode.asStateFlow()
    // A private prop which is responsible for holding the current state of the sign-in process.
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle) // Holds current state
    val signInState = _signInState.asStateFlow()
    //
    private val _errorToastChannel = MutableSharedFlow<String>()
    val errorToastChannel = _errorToastChannel.asSharedFlow()

    /**
     * An init-block which is responsible for checking for a pending authentication result.
     * Ensuring the sign-in flow completes when the user is redirected back to the app,
     */
    init {
        // Handle pending auth-results from ext. providers.
        auth.pendingAuthResult?.let { task ->
            _signInState.value = SignInState.Loading
            task.addOnSuccessListener { authResult ->
                _signInState.value = SignInState.Success(authResult)
                _isAuthenticated.value = true
                _photoUrl.value = auth.currentUser?.photoUrl
                _isGuestMode.value = false
            }.addOnFailureListener { e ->
                viewModelScope.launch {
                    _errorToastChannel.emit("Error: ${e.message}")
                }
                _signInState.value = SignInState.Error(e.message ?: "Unexpected error occurred!")
            }
        }
        // Observer authentication state changes to fetch user preferences.
        _isAuthenticated.onEach { authenticated ->
            if (authenticated) {
                fetchUpdateNotificationPreference()
            } else {
                _updateNotificationPreference.value = false
            }
        }.launchIn(viewModelScope)
    }


    /**
     * A function which is responsible for fetching the user's notification preference from Firestore.
     * The function is called automatically when the user logs in.
     */
    private fun fetchUpdateNotificationPreference() {
        viewModelScope.launch{
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                val preference = doc.getBoolean("receiveUpdates") ?: false
                _updateNotificationPreference.value = preference
            } catch (e: Exception) {
               Log.e(TAG, "Error fetching notification preference: ${e.message}", e)
                _updateNotificationPreference.value = false // Non-crit error, default to false.
            }
        }
    }

    /**
     * A function which is responsible for updating the user's notification preference in Firestore.
     *
     * @param isSubscribed A Boolean which is used to set the user's notification preference.
     */
    fun setUpdateNotificationPreference(isSubscribed: Boolean) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            try {
                _updateNotificationPreference.value = isSubscribed
                val userDocRef = firestore.collection("users").document(userId)
                val preference = mapOf("receiveUpdates" to isSubscribed)
                userDocRef.set(preference, SetOptions.merge()).await()
                if (isSubscribed) {
                    AppUpdateScheduler.scheduleWork(getApplication())
                    Log.i(TAG, "Work request scheduled for update notifications.")
                    _errorToastChannel.emit("In-app updating enabled!")
                } else {
                    AppUpdateScheduler.cancelWork(getApplication())
                    Log.i(TAG, "Work request cancelled for update notifications.")
                    _errorToastChannel.emit("In-app updating disabled!")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification preference: ${e.message}", e)
                _updateNotificationPreference.value = !isSubscribed
                _errorToastChannel.emit("Failed to save preference, check internet connectivity")
            }
        }
    }

    /**
     * A function which is responsible for initiating the google sign-in process with firebase.
     * The suspend function is launched in a [viewModelScope] to perform async network operations to
     * prevent blocking of the main thread.
     *
     * @param idToken The Google ID Token acquired from the google sign-in flow.
     */
    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _signInState.value = SignInState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                _signInState.value = SignInState.Success(authResult)
                _isAuthenticated.value = true
                _photoUrl.value = auth.currentUser?.photoUrl
                _isGuestMode.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error signing in with Google: ${e.message}", e)
                _signInState.value = SignInState.Error(e.message ?: "Google Sign-in Failed")
                _errorToastChannel.emit("Google sign-in failed, please try again.")
            }
        }
    }

    /**
     * A function which is responsible for initiating the GitHub sign-in process with Firebase.
     * It utilizes an [OAuthProvider] to configure the sign-in request and subsequently launches the
     * authentication flow on the provided [Activity].
     *
     * @param activity The [Activity] instance used to launch the sign-in flow.
     */
    fun signInWithGithub(activity: Activity) {
        _signInState.value = SignInState.Loading
        val provider = OAuthProvider.newBuilder("github.com").apply {
            scopes = listOf("user:email", "read:user")
            // Ensures user is prompted to select an account on sign-in.
            addCustomParameter("prompt", "select_account")
        }.build()

        auth.startActivityForSignInWithProvider(activity, provider)
            .addOnSuccessListener { authResult ->
                _signInState.value = SignInState.Success(authResult)
                _isAuthenticated.value = true
                _photoUrl.value = auth.currentUser?.photoUrl
                _isGuestMode.value = false
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error signing in with GitHub: ${e.message}", e)
                viewModelScope.launch {
                    _errorToastChannel.emit("GitHub sign-in failed, please try again.")
                }
                _signInState.value = SignInState.Error(e.message ?: "Unexpected error occurred!")
            }
    }

    /**
     * A function which is responsible for allowing signing in as a guest user.
     */
    fun signInAsGuest() {
        _signInState.value = SignInState.Loading
        _isGuestMode.value = true
        _isAuthenticated.value = false
        _signInState.value = SignInState.GuestSuccess
    }

    /**
     * A function which is responsible for signing out an active user.
     */
    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _photoUrl.value = null
        _isGuestMode.value = false
    }

    /**
     * A function which is responsible for resetting the state back to [SignInState.Idle]
     */
    fun resetSignInState() {
        _signInState.value = SignInState.Idle
    }

    companion object {
        /**
         * A synchronous function which is responsible for fetching the user's notification preference.
         *
         * @return A Boolean representing the user's notification preference.
         * @default Returns false if the user is not authenticated.
         */
        fun getUpdatePreferenceBlocking() : Boolean {
            val auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid ?: return false
            val firestore  = FirebaseFirestore.getInstance()
            val userDocRef = firestore.collection("users").document(userId)

            return try {
                val latch = CountDownLatch(1)
                var result = false
                userDocRef.get()
                    .addOnSuccessListener { document ->
                        result = document.getBoolean("receiveUpdates") ?: false
                        latch.countDown()
                    }
                    .addOnFailureListener {
                        latch.countDown()
                    }
                latch.await()
                result
            } catch (e: Exception) {
                Log.e("CIT - AuthViewModel", "Error fetching notification preference: ${e.message}", e)
                false
            }
        }
    }
}