package com.cturner56.streetwise_toolbox.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import com.cturner56.streetwise_toolbox.R
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import com.cturner56.streetwise_toolbox.destinations.Destination
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cturner56.streetwise_toolbox.viewmodel.AuthViewModel

/**
 * A composable which provides additional screens to navigate via a dropdown menu.
 * doc-ref: https://developer.android.com/develop/ui/compose/components/menu
 *
 * @param navController Used to handle navigation events when a [DropdownMenuItem] is clicked.
 */
@Composable
fun DropdownMenu(
    navController: NavController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isGuestMode by authViewModel.isGuestMode.collectAsState()
    val isSubscribed by authViewModel.updateNotificationPreference.collectAsState()

    val ic_send = painterResource(id= R.drawable.ic_send)
    val ic_dropdown = painterResource(id= R.drawable.ic_dropdown)
    val ic_info = painterResource(id= R.drawable.ic_info)
    val ic_repos = painterResource(id = R.drawable.ic_repos)
    val ic_account_mgmt = painterResource(id = R.drawable.ic_account_mgmt)

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
                    expanded = false
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("About") },
                leadingIcon = { Icon(painter = ic_info, contentDescription = null) },
                onClick = {
                    navController.navigate(Destination.About.route)
                    expanded = false
                }
            )
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Repository Spotlight") },
                leadingIcon = { Icon(painter = ic_repos, contentDescription = null) },
                onClick = {
                    navController.navigate(Destination.RepoSpotlight.route)
                    expanded = false
                }
            )
            HorizontalDivider()

            if (isAuthenticated) {
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Enable Updates")
                            Switch(
                                checked = isSubscribed,
                                onCheckedChange = { newPreferenceValue ->
                                    authViewModel.setUpdateNotificationPreference(newPreferenceValue)
                                }
                            )
                        }
                    },
                    onClick = { authViewModel.setUpdateNotificationPreference(!isSubscribed) }
                )
            }

            if (isAuthenticated || isGuestMode) {
                HorizontalDivider()
                DropdownMenuItem(
                text = {
                    val logoutText = if (isGuestMode) "Exist as Guest" else "Logout as User"
                    Text(logoutText)
                },
                leadingIcon = { Icon(painter = ic_account_mgmt, contentDescription = null) },
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                        expanded = false
                    }
                )
            }
        }
    }
}