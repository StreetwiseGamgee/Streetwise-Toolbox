package com.cturner56.streetwise_toolbox.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
 * A [ViewModel] which is responsible for handling user authentication logic.
 * The model exposes the [signInState] using [asStateFlow]. Using [viewModelScope] it launches
 * coroutines that are canceled when the ViewModel clears.
 */
class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null) // Checks current user
    val isAuthenticated = _isAuthenticated.asStateFlow()
    private val _photoUrl = MutableStateFlow(auth.currentUser?.photoUrl)
    val photoUrl = _photoUrl.asStateFlow()
    private val _isGuestMode = MutableStateFlow(false)
    private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle) // Holds current state
    val signInState = _signInState.asStateFlow()

    init {
        auth.pendingAuthResult?.let { task ->
            _signInState.value = SignInState.Loading
            task.addOnSuccessListener { authResult ->
                _signInState.value = SignInState.Success(authResult)
                _isAuthenticated.value = true
                _photoUrl.value = auth.currentUser?.photoUrl
                _isGuestMode.value = false
            }.addOnFailureListener { e ->
                _signInState.value = SignInState.Error(e.message ?: "Unexpected error occurred!")
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
                _signInState.value = SignInState.Error(e.message ?: "Unexpected error occurred!")
            }
        }
    }

    /**
     * A function which is responsible for initiating the github sign-in process with firebase.
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
    }

    /**
     * A function which is responsible for resetting the state back to [SignInState.Idle]
     */
    fun resetSignInState() {
        _signInState.value = SignInState.Idle
    }
}