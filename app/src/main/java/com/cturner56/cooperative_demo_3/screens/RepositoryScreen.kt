package com.cturner56.cooperative_demo_3.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.cturner56.cooperative_demo_3.db.AppDatabase
import com.cturner56.cooperative_demo_3.utils.NetworkStatus
import com.cturner56.cooperative_demo_3.viewmodel.GithubViewModel
import com.cturner56.cooperative_demo_3.R
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Purpose - A composable function which renders a Github repositories.
 * It observes the state from the [GithubViewModel] to display content.
 *
 * A loading indicator will remain should the device not have a local cache or...
 * The device is disconnected from the internet!
 *
 * Data retrieval is triggered by a [LaunchedEffect] when the screen is composed.
 *
 * @param githubViewModel - Responsible for fetching, and holding repository information.
 */
@Composable
fun RepositoryScreen(
    githubViewModel: GithubViewModel = viewModel()
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_repo),
                    contentDescription = "Add repository",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) { innerPadding ->

        ListRepositoryContent(
            githubViewModel = githubViewModel,
            modifier = Modifier.padding(innerPadding)
        )

        if (showDialog) {
            AddRepositoryDialog(
                onDismiss = { showDialog = false},
                onConfirm = {owner, repoName ->
                    showDialog = false
                    val dao = AppDatabase.getInstance(context).githubDao()
                    githubViewModel.addUserDefinedRepo(owner, repoName, dao)
                }
            )
        }
    }
}

@Composable
fun ListRepositoryContent(githubViewModel: GithubViewModel, modifier: Modifier = Modifier) {

    // Listens to state from view model
    val repositories = githubViewModel.repositoryListState.value
    val releases = githubViewModel.releasesState.value
    val error = githubViewModel.errorState.value

    val context = LocalContext.current
    val githubDao = AppDatabase.getInstance(context).githubDao()

    // Triggers initial data retrieval when composable first enters composition.
    LaunchedEffect(key1 = true) {
        githubViewModel.fetchFeaturedRepos(githubDao)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        val isLoading = repositories.isEmpty() && error == null
        when {
            isLoading -> { // Displays spinner when it initially fetched data.
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

            repositories.isNotEmpty() || error != null -> {
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.primary, // Fixes Accessibility
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                if (repositories.isNotEmpty()) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (owner: String, repoName: String) -> Unit
) {
    var owner by remember { mutableStateOf(TextFieldValue("")) }
    var repoName by remember { mutableStateOf(TextFieldValue("")) }
    val userSubmissionAccess = owner.text.isNotBlank() && repoName.text.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add new repository") },
        text = {
            Column {
                    OutlinedTextField(
                        value = owner,
                        onValueChange = { owner = it },
                        label = { Text("Owner example: 'RikkaApps'")},
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = repoName,
                        onValueChange = { repoName = it },
                        label = { Text("Repo example: 'Shizuku'")},
                        singleLine = true
                    )
                }
            },
        confirmButton = {
            Button(
                onClick = { onConfirm(owner.text.trim(), repoName.text.trim()) },
                enabled = userSubmissionAccess
            ) {
                Text("Add Submission")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel Submission")
            }
        }
    )
}

/**
 * A composable function which is responsible for displaying information pertaining to a Github repository.
 * Information is displayed in a card layout while providing hyperlinks to both the repo, and latest release.
 *
 * @param repository An object which contains a [GithubRepository]'s...
 * - Stars: The amount of stars given to a repo by other users on Github.
 * - Full name: i.e 'owner/repo'.
 * - Description: A description of the repository.
 * - Hyperlink to repository
 *
 * @param release An optional object which contains [RepositoryReleaseVersion]
 * - Date Published: The corresponding date and time of publication.
 * - Tag Name: The 'tag' associated with a release version i.e 'v.4.4-inject-s'
 */
@Composable
fun RepositoryCard(repository: GithubRepository, release: RepositoryReleaseVersion?) {
    val parts = repository.fullName.split('/').take(2)
    val owner = parts.getOrNull(0)
    val repositoryName = parts.getOrNull(1)

    Card() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_star),
                    contentDescription = "Star count",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = repository.starCount.toString() + " Stars",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(
                text = repository.fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = repository.description ?: "No description provided.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp)) // Divider between repository and release info.

            if (owner != null && repositoryName != null ) {
                HyperLinkText(url = repository.htmlUrl,
                    label = "View $owner's repository $repositoryName")
            }

        }
        release?.let { release ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Latest release for $repositoryName:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Published on: ${release.datePublished}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                HyperLinkText(url = release.htmlUrl, label = "View release: ${release.tagName}")
            }
        }
    }
}

/**
 * Purpose - A function which allows text to be used as a hyperlink.
 * @param label - The hyperlink text.
 * @param color - The color defined for the text.
 * @param TextDecoration - The underline defined for the text.
 * @param Modifier - Allows the hyperlink to be clickable, and redirects the user to the
 * respective website if connectivity is established. Otherwise, a toast notification is displayed.
 */
@Composable
fun HyperLinkText(url: String, label: String) {
    val context = LocalContext.current
    Text(
        text = label,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable {
            if (NetworkStatus.isOnline(context)) {
                val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                context.startActivity(intent)
            } else {
                Toast.makeText(
                    context,
                    "Please connect to the internet to view repository information.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    )
}