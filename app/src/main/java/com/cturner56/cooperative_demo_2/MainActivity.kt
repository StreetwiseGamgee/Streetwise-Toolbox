package com.cturner56.cooperative_demo_2

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cturner56.cooperative_demo_2.navigation.BottomNav
import com.cturner56.cooperative_demo_2.ui.theme.CooperativeDemo1DeviceStatisticsTheme
import com.cturner56.cooperative_demo_2.destinations.Destination
import com.cturner56.cooperative_demo_2.screens.AboutScreen
import com.cturner56.cooperative_demo_2.screens.BatteryScreen
import com.cturner56.cooperative_demo_2.screens.BuildScreen
import com.cturner56.cooperative_demo_2.screens.DropdownMenu
import com.cturner56.cooperative_demo_2.screens.FeedbackScreen
import com.cturner56.cooperative_demo_2.screens.MemoryScreen
import com.cturner56.cooperative_demo_2.utils.ManagePermissionState


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
           CooperativeDemo1DeviceStatisticsTheme {
               ManagePermissionState { isGranted, requestPermission ->  }
               val navController = rememberNavController()
               Scaffold(
                   topBar = {
                       TopAppBar(
                           title = { Text("Co-op Learning Demo #2")},
                           actions = {
                               DropdownMenu(navController)
                           }
                       )
                   },
                   bottomBar = {
                       BottomNav(navController = navController)
                   }
               ) { paddingValues ->
                   paddingValues.calculateBottomPadding()
                   Spacer(modifier = Modifier.padding(10.dp))
                   NavHost(
                       navController = navController,
                       startDestination = Destination.Battery.route,
                       modifier = Modifier.padding(paddingValues)
                   ) {
                       composable(Destination.Battery.route) {
                           BatteryScreen()
                       }
                       composable(Destination.Build.route) {
                           BuildScreen()
                       }
                       composable(Destination.Memory.route) {
                           MemoryScreen()
                       }
                       composable(Destination.About.route) {
                           AboutScreen()
                       }
                       composable(Destination.Feedback.route) {
                           FeedbackScreen()
                       }
                   }
               }
           }
        }
    }
}