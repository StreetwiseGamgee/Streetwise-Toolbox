package com.cturner56.cooperative_demo_1_device_statistics.screens

import androidx.compose.foundation.layout.Box
import com.cturner56.cooperative_demo_1_device_statistics.R
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import com.cturner56.cooperative_demo_1_device_statistics.destinations.Destination
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

// Derived from https://developer.android.com/develop/ui/compose/components/menu
@Composable
fun DropdownMenu(navController: NavController) {
    var expanded by remember { mutableStateOf(false) }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val ic_send = painterResource(id= R.drawable.ic_send)
    val ic_dropdown = painterResource(id= R.drawable.ic_dropdown)
    val ic_info = painterResource(id= R.drawable.ic_info)

    Box(
        modifier = Modifier
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(painter = ic_dropdown, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Send Feedback") },
                trailingIcon = { Icon(painter = ic_send, contentDescription = null) },
                onClick = {
                    navController.navigate(Destination.Feedback.route)
                }
            )

            HorizontalDivider()

            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(painter = ic_info, contentDescription = null) },
                onClick = {
                    navController.navigate(Destination.About.route)
                }
            )
        }
    }
}