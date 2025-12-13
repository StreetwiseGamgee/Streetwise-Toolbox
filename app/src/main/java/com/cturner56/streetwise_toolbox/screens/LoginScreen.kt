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

                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        .build()

                    val request: GetCredentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(context, request)
                    val credential = result.credential

                    if (credential is GoogleIdTokenCredential) {
                        // If successful it'll pass the token to the ViewModel
                        authViewModel.signInWithGoogle(credential.idToken)
                    } else {
                        Log.e("LoginScreen - CIT", "Credential Type Unsupported")
                        Toast.makeText(context, "Unsupported credential type.", Toast.LENGTH_LONG)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Sign-in Failed: ${e.message}", Toast.LENGTH_LONG)
                }
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Sign in with Google")
    }
}