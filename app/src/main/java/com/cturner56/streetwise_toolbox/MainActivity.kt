package com.cturner56.streetwise_toolbox

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cturner56.streetwise_toolbox.navigation.BottomNav
import com.cturner56.streetwise_toolbox.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import com.cturner56.streetwise_toolbox.destinations.Destination
import com.cturner56.streetwise_toolbox.screens.AboutScreen
import com.cturner56.streetwise_toolbox.screens.BatteryScreen
import com.cturner56.streetwise_toolbox.screens.BuildScreen
import com.cturner56.streetwise_toolbox.navigation.DropdownMenu
import com.cturner56.streetwise_toolbox.screens.FeedbackScreen
import com.cturner56.streetwise_toolbox.screens.LoginScreen
import com.cturner56.streetwise_toolbox.screens.MemoryScreen
import com.cturner56.streetwise_toolbox.screens.RepositoryScreen
import com.cturner56.streetwise_toolbox.utils.ManagePermissionState
import com.cturner56.streetwise_toolbox.viewmodel.AuthViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           CooperativeDemo1DeviceStatisticsTheme {
               val authViewModel: AuthViewModel = viewModel()
               val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
               val startDestination = if (isAuthenticated) "MainScaffold" else "LoginScreen"
               val navController = rememberNavController()

               NavHost(
                   navController = navController,
                   startDestination = startDestination
               ) {
                   composable("LoginScreen") {
                       LoginScreen(navController = navController, authViewModel = authViewModel)
                   }
                   composable("MainScaffold") {
                       MainActivityScaffold(mainNavController = navController)
                   }
               }
           }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityScaffold(mainNavController: NavController) {
    ManagePermissionState { isGranted, requestPermission ->
        val innerNavController = rememberNavController()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Streetwise's Toolbox")},
                    actions = {
                        DropdownMenu(mainNavController)
                    }
                )
            },
            bottomBar = {
                BottomNav(navController = mainNavController)
            }
        ) { paddingValues ->
            paddingValues.calculateBottomPadding()
            Spacer(modifier = Modifier.padding(10.dp))
            NavHost(
                navController = innerNavController,
                startDestination = Destination.Build.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Destination.Battery.route) { BatteryScreen() }
                composable(Destination.Build.route) { BuildScreen(
                    isShizukuGranted = isGranted,
                    onRequestShizukuPermission = requestPermission
                )}

                composable(Destination.Memory.route) { MemoryScreen() }
                composable(Destination.About.route) { AboutScreen() }
                composable(Destination.Feedback.route) { FeedbackScreen() }
                composable(Destination.RepoSpotlight.route) { RepositoryScreen() }
            }
        }
    }
}