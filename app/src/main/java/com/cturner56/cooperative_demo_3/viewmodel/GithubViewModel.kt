package com.cturner56.cooperative_demo_3.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cturner56.cooperative_demo_3.api.Api
import com.cturner56.cooperative_demo_3.api.model.GithubRepository
import com.cturner56.cooperative_demo_3.api.model.RepositoryReleaseVersion
import com.cturner56.cooperative_demo_3.db.GithubDao
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A [ViewModel] which is responsible for retrieving and caching relevant repository information,
 * as well as release details for a pre-defined list of public Github repositories.
 *
 * It fetches information from a local Room database for caching, and
 * utilizes Github's RestfulAPI to retrieve and update such information.
 *
 * It exposes subsequently retrieved data, and or errors through the use of [State] objects.
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
     * A function which is responsible for calling on, and loading repository related information.
     *
     * Providing a main entry point for data loading. It initiates a coroutine that:
     * 1. First calls on [loadFromCache] in an attempt to retrieve data which has been stored
     * locally via Room-db.
     *
     * 2. Subsequently it calls on [refreshFromNetwork] in attempt to refresh data
     * from respective online sources.
     *
     * 3. In the event the refresh fails, an appropriate error message is displayed while still
     * providing offline access.
     *
     * @param githubDao The Data Access Object for interacting with the local Room database.
     */
    fun fetchFeaturedRepos(githubDao: GithubDao) {
        viewModelScope.launch {
            loadFromCache(githubDao)

            val wasRefreshSuccessful = refreshFromNetwork(githubDao)

            if (!wasRefreshSuccessful) {
                _errorState.value = "[OFFLINE]\nDisplaying Local Information"
            }
        }
    }

    /**
     *  A function which attempts to retrieve cached repository information from the
     *  local Room database. If data is found, [repositoryListState] and [releasesState] are displayed.
     *
     *  [repositoryListState]: Populates with repositories which are stored in Room.
     *  [releasesState]: Populates with releases which are stored in Room.
     *  [errorState] Updates in case of any errors. Providing messages based on respective error caught.
     */
    private suspend fun loadFromCache(githubDao: GithubDao) {
        try {
            val (cachedRepositories, cachedReleasesMap) = withContext(Dispatchers.IO) {
                val repos = githubDao.getAllRepositories()
                val releases = repos.associate { repo ->
                    repo.fullName to githubDao.getReleasesForRepository(repo.fullName).firstOrNull()
                }
                repos to releases
            }
            if (cachedRepositories.isNotEmpty()) {
                Log.d("CIT - GithubViewModel", "Successfully loaded repositories from cache.")
                _repositoryListState.value = cachedRepositories
                _releasesState.value = cachedReleasesMap
            } else {
                Log.d(
                    "CIT - GithubViewModel",
                    "Local instance is empty. Attempting to fetch via network."
                )
            }
        } catch (e: Exception) {
            Log.e(
                "CIT - GithubViewModel",
                "Failed to load repository information from offline cache.", e
            )
        }
    }

    /**
     * A function which attempts to refresh repository data from the network.
     * On success, it returns true, and subsequently updates the UI and cached data.
     * On failure it returns false.
     *
     * [repositoryListState]: Populates with repos which we're successfully fetched.
     * [releasesState]: Populates latest release for each successfully fetched repository.
     * [errorState]: Clears error state on a new attempt, but not set such on failure.
     */
    private suspend fun refreshFromNetwork(githubDao: GithubDao): Boolean {
        return try {
            _errorState.value = null // Clears any existing error messages.
            // Creates a list of async jobs for each repository.
            val repositories = withContext(Dispatchers.IO) {
                val repositoryRetrieval = featuredRepositories.map { fullName ->
                    async {
                        try {
                            val (owner, repo) = fullName.split("/")
                            Api.retrofitService.getRepository(owner, repo)
                        } catch (e: Exception) {
                        Log.e(
                            "CIT - GithubViewModel", "Failed to refresh repository " +
                            "from online resource: $fullName", e)
                            null
                        }
                    }
                }
                repositoryRetrieval.awaitAll().filterNotNull()
            }

            if (repositories.isEmpty()) {
                return false
            }
            val releasesMap = withContext(Dispatchers.IO) {
                val releaseRetrieval = repositories.map { repo ->
                    async {
                        try {
                            val (owner, name) = repo.fullName.split("/")
                            val latestRelease =
                                Api.retrofitService.getReleases(owner, name).firstOrNull()
                            latestRelease?.repoFullName = repo.fullName
                            repo.fullName to latestRelease
                        } catch (e: Exception) {
                            repo.fullName to null
                        }
                    }
                }
                releaseRetrieval.awaitAll().toMap()
            }
            _repositoryListState.value = repositories // Update UI State
            _releasesState.value = releasesMap // Updates UI State

            withContext(Dispatchers.IO) { // Writes newly retrieved data to local Room -db instance.
                githubDao.insertRepositories(repositories)
                // Ensures null releases aren't inserted into the database.
                githubDao.insertReleases(releasesMap.values.filterNotNull())
                Log.d(
                    "CIT - GithubViewModel",
                    "Information successfully retrieved from online sources, " +
                            "updated local cache with ${repositories.size} repos!"
                )
            }
            true
        } catch (e: Exception) {
                Log.e("CIT - GithubViewModel", "Unable to refresh data, showing cached content", e)
            false
        }
    }
}