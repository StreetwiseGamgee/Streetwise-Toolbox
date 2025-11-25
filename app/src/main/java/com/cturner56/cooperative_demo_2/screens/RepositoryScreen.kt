package com.cturner56.cooperative_demo_2.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cturner56.cooperative_demo_2.viewmodel.GithubViewModel

/**
 * Purpose - A composable function which renders a Github repository, making use of [GithubViewModel]
 * to fetch relevant information pertaining to the repo itself, and the latest release provided.
 * @param githubViewModel - Responsible for fetching, and holding repository information.
 */
@Composable
fun RepositoryScreen(
    githubViewModel: GithubViewModel = viewModel()
) {
    // Listens to state from view model
    val repository = githubViewModel.repositoryState.value
    val release = githubViewModel.releaseState.value
    val error = githubViewModel.errorState.value

    LaunchedEffect(key1 = true) {
        githubViewModel.fetchRepoData(owner = "ZG089", repo = "Re-Malwack")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        if (repository == null && error == null) { // Loading State.
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Fetching repository information, please wait.")
        }
        error?.let { // Error State, should information not load properly.
            Text(text = it, color = Color.Red, style = MaterialTheme.typography.bodyLarge)
        }
            Card() {
                repository?.let { repo ->
                    Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.Start) {
                        Text(
                            text = repo.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = repo.description ?: "No description provided.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer (modifier = Modifier.height(4.dp))
                        HyperLinkText(url = repo.htmlUrl, label = "View Repository on Github")
                    }
                release?.let {release ->
                    Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.Start) {
                        Text(
                            text = "Latest released version: ${release.tagName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer (modifier = Modifier.height(4.dp))
                        HyperLinkText(url = repo.htmlUrl, label = "View release on Github")
                    }
                }
            }
        }
    }
}

/**
 * Purpose - A function which allows text to be used as a hyperlink.
 * @param label - The hyperlink text.
 * @param color - The color defined for the text.
 * @param TextDecoration - The underline defined for the text.
 * @param Modifier - Allows the hyperlink to be clickable, and redirects the user to the respective website.
 */
@Composable
fun HyperLinkText(url: String, label: String) {
    val context = LocalContext.current
    Text(
        text = label,
        color = Color.Blue,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        }
    )
}