package com.cturner56.streetwise_toolbox.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cturner56.streetwise_toolbox.api.model.GithubRepository
import com.cturner56.streetwise_toolbox.api.model.RepositoryReleaseVersion
import com.cturner56.streetwise_toolbox.utils.NetworkStatus
import com.cturner56.streetwise_toolbox.viewmodel.GithubViewModel
import com.cturner56.streetwise_toolbox.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Purpose - A composable function manages the overall UI layout and user-interactions.
 *
 * Utilizing a [Scaffold] it provides the layout structure which includes a [FloatingActionButton]
 * to add new GitHub repositories. When the action button is clicked, a [AddRepositoryDialog] will
 * pop-up and prompt a user to fill in repository information such as the owner and repo name.
 *
 * @param githubViewModel - Responsible for fetching and holding repository information.
 */
@Composable
fun RepositoryScreen(
    githubViewModel: GithubViewModel = viewModel(
        viewModelStoreOwner = LocalContext.current as ComponentActivity
    )
) {
    var showDialog by remember { mutableStateOf(false) }

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
                    githubViewModel.addRepository(owner, repoName)
                }
            )
        }
    }
}

/**
 * A function which is responsible for handling the UI-state for loading and errors.
 *
 * It observes the state from the [GithubViewModel] to show...
 * 1. A loading indicator upon initial retrieval.
 * 2. A list of repositories (whether cached or retrieved)
 * 3. A corresponding error message if applicable.
 * Data retrieval is triggered by [LaunchedEffect] when the composable initially enters composition.
 *
 * @param githubViewModel The ViewModel instance which is responsible for providing the data.
 */
@Composable
fun ListRepositoryContent(githubViewModel: GithubViewModel, modifier: Modifier = Modifier) {

    // Listens to state from view model
    val repositories = githubViewModel.repositoryListState.value
    val releases = githubViewModel.releasesState.value
    val error = githubViewModel.errorState.value

    // Listens to the offline togglable state.
    val isNetworkRefreshDisabled = githubViewModel.isNetworkRefreshDisabled.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        )  {
            Text(
                text = "Disable Network Refresh",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = isNetworkRefreshDisabled,
                onCheckedChange = { isChecked ->
                    githubViewModel.toggleNetworkRefresh(isChecked)
                }
            )
        }

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
                            RepositoryCard(
                                repository = repo,
                                release = release,
                                onDelete = {
                                    githubViewModel.deleteRepository(repo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * A composable which is responsible for displaying a pop-up dialog which allows the user to add a
 * GitHub repository via it's owner's name, and the repository name.
 *
 * @param onDismiss A Lambda function which is invoked when a user dismisses the dialog.
 * @param onConfirm A Lambda function which is invoked with the corresponding inputs 'owner/repo'.
 */
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
 *
 * @param onDelete A lambda function which is invoked when the delete icon is clicked.
 */
@Composable
fun RepositoryCard(
    repository: GithubRepository,
    release:
    RepositoryReleaseVersion?,
    onDelete: () -> Unit
    ) {
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
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = repository.starCount.toString() + " Stars",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(onClick = onDelete){
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete_repo),
                            contentDescription = "Delete repository",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
 * Allows the hyperlink to be clickable, and redirects the user to the
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