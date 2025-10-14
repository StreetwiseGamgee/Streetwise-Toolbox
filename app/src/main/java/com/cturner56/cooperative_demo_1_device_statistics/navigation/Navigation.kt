package com.cturner56.cooperative_demo_1_device_statistics.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cturner56.cooperative_demo_1_device_statistics.R
import com.cturner56.cooperative_demo_1_device_statistics.destinations.Destination
import androidx.compose.material3.Icon
import androidx.compose.material3.Text

@Composable
fun BottomNav(navController: NavController){
    NavigationBar {
        val navBackStackEntry = navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry.value?.destination

        val ic_battery = painterResource(id= R.drawable.ic_battery)
        val ic_build = painterResource(id= R.drawable.ic_build)

        NavigationBarItem(
            selected = currentDestination?.route == Destination.Battery.route,
            onClick = {
                    navController.navigate(Destination.Battery.route){
                        popUpTo(Destination.Battery.route)
                        launchSingleTop = true
                    }},
                icon = { Icon(painter = ic_battery, contentDescription = null)},
                label = { Text(text = Destination.Battery.route) }
        )

        NavigationBarItem(
            selected = currentDestination?.route == Destination.Build.route,
            onClick = {
                navController.navigate(Destination.Build.route){
                    popUpTo(Destination.Build.route)
                    launchSingleTop = true
                }},
            icon = { Icon(painter = ic_build, contentDescription = null)},
            label = { Text(text = Destination.Build.route) }
        )

        NavigationBarItem(
            selected = currentDestination?.route == Destination.Memory.route,
            onClick = {
                navController.navigate(Destination.Memory.route){
                    popUpTo(Destination.Memory.route)
                    launchSingleTop = true
                }},
            icon = { Icon(painter = ic_build, contentDescription = null)},
            label = { Text(text = Destination.Memory.route) }
        )
    }
}