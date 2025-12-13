package com.cturner56.streetwise_toolbox.screens

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
                Toast.makeText(context, "Google Account Authenticated", Toast.LENGTH_SHORT).show()

                navController.navigate("MainScaffold") { // On success, navigates to main content.
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
                GoogleAuthButton(authViewModel = authViewModel, context = context)
            }
        }
    }
}

/**
 * A composable function which is responsible for displaying the 'Sign in with Google" Button.
 *
 * When clicked, it opens up a Google sign in flow using Android's credential manager.
 * For security a nonce is first generated. Next, it requests a ID token from Google, and subsequently
 * passes the token to the [AuthViewModel] for authentication.
 *
 * @param authViewModel The [AuthViewModel] instance responsible for handling the sign-in state.
 * @param context The [Context] instance used to create the [CredentialManager] and access resources.
 */
@OptIn(ExperimentalStdlibApi::class)
@Composable
fun GoogleAuthButton(
    authViewModel: AuthViewModel,
    context: Context
) {
    val coroutineScope = rememberCoroutineScope()
    Button (
        onClick = {
            coroutineScope.launch {
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
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Sign in with Google")
    }
}

/**
 * Helper function which is responsible for converting a ByteArray to a hex string.
 *
 * @return The hex string representation of the ByteArray.
 * @see ByteArray
 */
private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }