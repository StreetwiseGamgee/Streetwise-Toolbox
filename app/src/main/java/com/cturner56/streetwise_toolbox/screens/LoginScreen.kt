package com.cturner56.streetwise_toolbox.screens

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cturner56.streetwise_toolbox.R
import com.cturner56.streetwise_toolbox.viewmodel.AuthViewModel
import com.cturner56.streetwise_toolbox.viewmodel.SignInState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.SecureRandom
import androidx.credentials.CustomCredential

/**
 * A composable function which is responsible for displaying the login screen.
 *
 * It uses the [AuthViewModel] to observe the state of the sign-in process. Once authenticated, the
 * UI updates to the main content. Should any errors occur during the process, an error message will
 * display and the process will be reset.
 *
 * @param navController The [NavController] instance used to navigate between screens.
 * @param authViewModel The [AuthViewModel] instance responsible for handling the sign-in state.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val signInState by authViewModel.signInState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(signInState) {
        when (val state = signInState) {
            is SignInState.Success -> {
                Log.d("CIT - LoginScreen", "Login / Signup Successful")
                Toast.makeText(context, "Account Authenticated Successfully", Toast.LENGTH_SHORT).show()

                navController.navigate("MainScaffold") { // On success, navigates to main content.
                    popUpTo("Login") { inclusive = true}
                }
                authViewModel.resetSignInState()
            }
            is SignInState.GuestSuccess -> {
                Log.d("CIT - LoginScreen", "Guest Login Successful")
                Toast.makeText(context, "Guest Login Successful", Toast.LENGTH_SHORT).show()

                navController.navigate("MainScaffold") {
                    popUpTo("Login") { inclusive = true}
                }
                authViewModel.resetSignInState()
            }
            is SignInState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                Log.w("CIT - LoginScreen", "Account Processing Unsuccessful: ${state.message}")
                authViewModel.resetSignInState() // Reset for subsequent attempts on failure.
            } else -> Unit // Do nothing for loading states.
        }
    }

    Box (
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (signInState is SignInState.Loading) {
            CircularProgressIndicator()
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val coroutineScope = rememberCoroutineScope()
                val activity = LocalContext.current as Activity

                // Google Auth Button
                AuthButton(
                    text = "Sign in with Google",
                    icon = painterResource(id = R.drawable.ic_android),
                    onClick = {
                        coroutineScope.launch {
                            signInWithGoogleCredentials(context, authViewModel)
                        }
                    }
                )
                // Github Auth Button
                AuthButton(
                    text = "Sign in with GitHub",
                    icon = painterResource(id = R.drawable.github_mark),
                    onClick = {
                        authViewModel.signInWithGithub(activity)
                    }
                )
                HorizontalDivider(modifier = Modifier .padding(12.dp))
                // Guest Sign-in
                AuthButton(
                    text = "Continue as Guest",
                    icon = painterResource(id = R.drawable.ic_guest),
                    onClick = {
                        authViewModel.signInAsGuest()
                    }
                )
            }
        }
    }
}

/**
 * A reusable composable function which is responsible for displaying a
 * button for account authentication.
 */
@Composable
fun AuthButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row( verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(text)
        }
    }
}

/**
 * A private function which is responsible for providing Google's authentication logic.
 *
 * It initiates a Google sign in flow using Android's credential manager.
 * For security a nonce is first generated. Next, it requests a ID token from Google, and subsequently
 * passes the token to the [AuthViewModel] for authentication.
 *
 * doc-ref:
 * https://developer.android.com/identity/sign-in/credential-manager-siwg
 *
 * @param authViewModel The [AuthViewModel] instance responsible for handling the sign-in state.
 * @param context The [Context] instance used to create the [CredentialManager] and access resources.
 */
private suspend fun signInWithGoogleCredentials(context: Context, authViewModel: AuthViewModel
) {
    try {
        val credentialManager = CredentialManager.create(context)
        val rawNonce = ByteArray(16)
        SecureRandom().nextBytes(rawNonce)
        val nonce = rawNonce.toHexString()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setNonce(nonce)
            .setAutoSelectEnabled(false) // Explicitly disable auto-selection
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(context, request)
        val credential = result.credential

        if (credential is GoogleIdTokenCredential) {
            // If successful it'll pass the token to the ViewModel
            authViewModel.signInWithGoogle(credential.idToken)
        } else if (credential is CustomCredential && credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                authViewModel.signInWithGoogle(idToken)
            } catch (e: Exception) {
                Log.e("LoginScreen - CIT", "Failed to create GoogleIdTokenCredential from CustomCredential", e)
            }
        } else {
            Log.e("LoginScreen - CIT", "Credential Type Unsupported: ${credential::class.java.name}")
            Toast.makeText(context, "Unsupported credential type.", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Sign-in Failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

/**
 * Helper function which is responsible for converting a ByteArray to a hex string.
 *
 * @return The hex string representation of the ByteArray.
 * @see ByteArray
 */
private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }