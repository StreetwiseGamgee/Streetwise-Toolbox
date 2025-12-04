package com.cturner56.cooperative_demo_3.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cturner56.cooperative_demo_3.api.Api
import com.cturner56.cooperative_demo_3.api.model.GithubRepository
import com.cturner56.cooperative_demo_3.api.model.RepositoryReleaseVersion
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.CancellationException

/**
 * A [ViewModel] which exposes relevant repo information, and release details for a public Github repository.
 * It uses Github's Rest-API to retrieve such information.
 */
class GithubViewModel : ViewModel() {

    // Holds the state for the list of fetched repositories.
    private val _repositoryListState = mutableStateOf<List<GithubRepository>>(emptyList())
    val repositoryListState: State<List<GithubRepository>> = _repositoryListState

    // Holds the state for each repositories latest release.
    private val _releasesState = mutableStateOf<Map<String, RepositoryReleaseVersion?>>(emptyMap())
    val releasesState: State<Map<String, RepositoryReleaseVersion?>> = _releasesState

    // Holds the state for any error messages that could occur during retrieval.
    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    // Predefined list of "owner/repo" which represents each repository to be featured.
    private val featuredRepositories = listOf(
        "ZG089/Re-Malwack",
        "5ec1cff/TrickyStore",
        "KOWX712/Tricky-Addon-Update-Target-List",
        "KOWX712/PlayIntegrityFix",
        "Dr-TSNG/ZygiskNext",
        "j-hc/zygisk-detach",
        "sidex15/susfs4ksu-module"
    )

    /**
     * A function which is responsible for retrieving repository information pertaining to repos
     * found inside the [featuredRepositories] list.
     *
     * It launches a coroutine which makes two async tasks in parallel:
     * 1. Retrieves information pertaining to the repository itself.
     * 2. Retrieves release information pertaining to the repo.
     *
     * The function updates the following states:
     * [repositoryListState]: Populates with repos which we're successfully fetched.
     * [releasesState]: Populates latest release for each successfully fetched repository.
     * [errorState] Updates in case of any errors. Providing messages based on respective error caught.
     *
     * In the event some repositories fail to load, an [errorState] will indicate how many we're failed
     * to be retrieved.
     *
     * @throws CancellationException if the viewModelScope is canceled.
     */
    fun fetchFeaturedRepos() {
        viewModelScope.launch {
            try {
                // Clears previous data, initiates loading.
                _repositoryListState.value = emptyList()
                _releasesState.value = emptyMap()
                _errorState.value = null

                // fetch repository information
                val repositoryRetrieval = featuredRepositories.map { fullName ->
                    async {
                        try {
                            val (owner, repo) = fullName.split("/")
                            Api.retrofitService.getRepository(owner, repo)
                        } catch (e: IOException) {
                            Log.e("CIT - GithubViewModel", "Network error fetching repo $fullName: ${e.message}")
                            null
                        } catch (e: HttpException) {
                            Log.e("CIT - GithubViewModel", "API error fetching repo $fullName: ${e.message}")
                            null
                        }
                    }
                }
                val repositoryResults = repositoryRetrieval.awaitAll()
                val repositories = repositoryResults.filterNotNull()
                _repositoryListState.value = repositories

                // Set error state if some repositories failed to load
                if (repositories.size < featuredRepositories.size) {
                    val failedCount = featuredRepositories.size - repositories.size
                    _errorState.value = "Failed to load $failedCount repositories. Please check your connection."
                }

                // Fetch release information for successfully loaded repositories
                val releaseRetrieval = repositories.map { repo ->
                    async {
                        try {
                            val (owner, name) = repo.fullName.split("/")
                            val releases = Api.retrofitService.getReleases(owner, name)
                            // On success, returns a pair containing the name and release
                            repo.fullName to releases.firstOrNull()
                        } catch (e: HttpException) {
                            Log.w("CIT - GithubViewModel", "No releases available for ${repo.fullName}", e)
                            // On failure, returns a pair of the name and null.
                            repo.fullName to null
                        } catch (e: IOException) {
                            Log.w("CIT - GithubViewModel", "Network error fetching releases for ${repo.fullName}", e)
                            repo.fullName to null
                        }
                    }
                }
                _releasesState.value = releaseRetrieval.awaitAll().toMap()

            } catch (e: CancellationException) {
                throw e // re-throw it to let the coroutine framework handle it.
            } catch (e: Exception) {
                // For all other unexpected exceptions, set the error state.
                _errorState.value = "An unexpected error has occurred: ${e.message}"
                Log.e("CIT - GithubViewModel", "An unexpected error has occurred", e)
            }
        }
    }
}