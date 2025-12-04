package com.cturner56.cooperative_demo_3.screens

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cturner56.cooperative_demo_3.api.model.GithubRepository
import com.cturner56.cooperative_demo_3.api.model.RepositoryReleaseVersion
import com.cturner56.cooperative_demo_3.viewmodel.GithubViewModel

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
    val repositories = githubViewModel.repositoryListState.value
    val releases = githubViewModel.releasesState.value
    val error = githubViewModel.errorState.value

    LaunchedEffect(key1 = true) {
        githubViewModel.fetchFeaturedRepos()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        when {
            repositories.isEmpty() && error == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {// Loading State.
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fetching repository information, please wait.")
                }

            }

            error != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(repositories) { repo ->
                        val release = releases[repo.fullName]
                        RepositoryCard(repository = repo, release = release)
                    }
                }
            }
        }
    }
}

/**
 * A composable function which is responsible for displaying information pertaining to a Github repository.
 * Information is displayed in a card layout while providing hyperlinks to both the repo, and latest release.
 *
 * @param repository An object which contains a [GithubRepository]'s name, description, and url.
 * @param release An optional object which contains [RepositoryReleaseVersion] pertaining to a repo.
 */
@Composable
fun RepositoryCard(repository: GithubRepository, release: RepositoryReleaseVersion?) {
    Card() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = repository.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = repository.description ?: "No description provided.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            HyperLinkText(url = repository.htmlUrl, label = "View Repository on Github")
        }
        release?.let { release ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Latest released version: ${release.tagName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                HyperLinkText(url = release.htmlUrl, label = "View release on Github")
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