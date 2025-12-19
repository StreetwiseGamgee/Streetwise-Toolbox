package com.cturner56.streetwise_toolbox

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.cturner56.streetwise_toolbox.navigation.BottomNav
import com.cturner56.streetwise_toolbox.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import com.cturner56.streetwise_toolbox.destinations.Destination
import com.cturner56.streetwise_toolbox.screens.AboutScreen
import com.cturner56.streetwise_toolbox.screens.BatteryScreen
import com.cturner56.streetwise_toolbox.screens.BuildScreen
import com.cturner56.streetwise_toolbox.navigation.DropdownMenu
import com.cturner56.streetwise_toolbox.screens.LoginScreen
import com.cturner56.streetwise_toolbox.screens.FeedbackScreen
import com.cturner56.streetwise_toolbox.screens.MemoryScreen
import com.cturner56.streetwise_toolbox.screens.RepositoryScreen
import com.cturner56.streetwise_toolbox.utils.ManagePermissionState
import com.cturner56.streetwise_toolbox.viewmodel.AuthViewModel

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           CooperativeDemo1DeviceStatisticsTheme {
               val authViewModel: AuthViewModel = viewModel()
               val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
               val startDestination = if (isAuthenticated) "MainScaffold" else
                   Destination.LoginScreen.route

               val navController = rememberNavController()

               NavHost(
                   navController = navController,
                   startDestination = startDestination
               ) {
                   composable(Destination.LoginScreen.route) {
                       LoginScreen(navController = navController, authViewModel = authViewModel)
                   }
                   composable("MainScaffold") {
                       MainActivityScaffold(
                           authViewModel = authViewModel,
                           onLogout = {
                               navController.navigate("Login") {
                                   popUpTo("Login") { inclusive = true }
                                   launchSingleTop = true // Prevents multiple instances of the same screen.
                               }
                           }
                       )
                   }
               }
           }
        }
    }
}

/**
 * A composable function which is responsible for displaying the main content of the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScaffold(authViewModel: AuthViewModel, onLogout: () -> Unit) {
    val photoUrl by authViewModel.photoUrl.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        authViewModel.errorToastChannel.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    ManagePermissionState(
        onServiceNotRunning = {
            Toast.makeText(context,
                "Shizuku isn't running, click 'request' to see how-to video",
                Toast.LENGTH_LONG).show()
        }
    ) { _, _ ->
        val innerNavController = rememberNavController()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Streetwise's Toolbox")},
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (photoUrl != null) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(id = R.drawable.ic_download_pfp)
                                )
                            }
                        }
                        DropdownMenu(
                            navController = innerNavController,
                            authViewModel = authViewModel,
                            onLogout = onLogout
                        )
                    }
                )
            },
            bottomBar = {
                BottomNav(navController = innerNavController)
            }
        ) { paddingValues ->
            NavHost(
                navController = innerNavController,
                startDestination = Destination.Build.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Destination.Battery.route) { BatteryScreen() }
                composable(Destination.Build.route) { BuildScreen()}

                composable(Destination.Memory.route) { MemoryScreen() }
                composable(Destination.About.route) { AboutScreen() }
                composable(Destination.Feedback.route) { FeedbackScreen() }
                composable(Destination.RepoSpotlight.route) { RepositoryScreen() }
            }
        }
    }
}