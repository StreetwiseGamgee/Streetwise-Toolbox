package com.cturner56.streetwise_toolbox.screens

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import android.util.Log
import android.widget.Toast
import android.widget.VideoView
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
import androidx.compose.ui.viewinterop.AndroidView
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
 * A composable function which is responsible for rendering and looping a video background.
 * Utilizing [AndroidView] to play the raw video resource. The function explicitly handles aspect
 * ratios to ensure minimal whitespace is displayed.
 * Credits for video / gif author:
 *
 * <a href="https://pixabay.com/users/dhiru6801-17848812/?utm_source=link-attribution&utm_medium=referral&utm_campaign=animation&utm_content=18050">Dhiru Bhai
 * </a> from <a href="https://pixabay.com//?utm_source=link-attribution&utm_medium=referral&utm_campaign=animation&utm_content=18050">Pixabay</a>
 * License:
 * https://pixabay.com/service/license-summary/
 *
 * GIF-Format was converted to .mp4 using Handbrake (FOSS Video Converter)
 * Prior the size was 90+ mb and after conversion it's 2.13 mb
 *
 * @param modifier The modifier which is applied to the layout.
 * @param videoResId The resource id affiliated with the raw video file [R.raw.login_bg_video].
 */
@Composable
fun VideoBackground(
    modifier: Modifier = Modifier,
    @androidx.annotation.RawRes videoResId: Int
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                setZOrderOnTop(false) // Prevents background from overlaying on-top of buttons.

                val uri = Uri.parse("android.resource://${context.packageName}/$videoResId")
                setVideoURI(uri)

                setOnPreparedListener { mediaPlayer ->

                    // Ensures video loops while playing.
                    mediaPlayer.isLooping = true

                    // Fetch video dimensions
                    val videoWidth = mediaPlayer.videoWidth.toFloat()
                    val videoHeight = mediaPlayer.videoHeight.toFloat()

                    // Fetch screen dimensions
                    val viewWidth = width.toFloat()
                    val viewHeight = height.toFloat()

                    // Calculates ratios
                    val videoRatio = videoWidth / videoHeight
                    val screenRatio = viewWidth / viewHeight

                    // Calculates scale required for x and y.
                    val calculatedScaleX = videoRatio / screenRatio
                    val calculatedScaleY = screenRatio / videoRatio

                    // Scale video to ensure there's no whitespace.
                    val maxScale = kotlin.math.max(calculatedScaleX, calculatedScaleY)
                    this.scaleX = maxScale * 1.5f // Forces scale to fill space.
                    this.scaleY = maxScale * 1.5f

                    this.rotation = 90f // Rotate the video 90 degrees.
                    start()
                }
            }
        }
    )
}

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

        VideoBackground(
            modifier = Modifier.fillMaxSize(),
            videoResId = R.raw.login_bg_video
        )

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