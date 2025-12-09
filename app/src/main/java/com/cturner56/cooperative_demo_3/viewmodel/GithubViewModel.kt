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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    fun fetchFeaturedRepos(githubDao: GithubDao) {
        viewModelScope.launch {
            // Attempts to load repository data from cached entries first.
            try {
                val cachedRepositories = withContext(Dispatchers.IO) {githubDao.getAllRepositories() }
                if (cachedRepositories.isNotEmpty()) {
                    _repositoryListState.value = cachedRepositories

                    val cachedReleasesMap = cachedRepositories.associate { repo ->
                        repo.fullName to withContext(Dispatchers.IO) {
                            githubDao.getReleasesForRepository(repo.fullName).firstOrNull()
                        }
                    }
                    _releasesState.value = cachedReleasesMap
                }
            } catch(e: Exception) {
                Log.e("CIT - GithubViewModel", "Failed to load repository information from offline cache.", e)
            }

            try {
                val repositoryRetrieval = featuredRepositories.map { fullName ->
                    async(Dispatchers.IO) {
                        try{
                            val (owner, repo) = fullName.split("/")
                            Api.retrofitService.getRepository(owner, repo)
                        } catch (e: Exception) {
                            null
                        }
                    }
                }
                val repositories = repositoryRetrieval.awaitAll().filterNotNull()

                val releaseRetrieval = repositories.map { repo ->
                    async(Dispatchers.IO) {
                        try{
                            val (owner, name) = repo.fullName.split("/")
                            val latestRelease = Api.retrofitService.getReleases(owner, name).firstOrNull()
                            repo.fullName to latestRelease
                        } catch (e: Exception) {
                           repo.fullName to null
                        }
                    }

                }
                val releasesMap = releaseRetrieval.awaitAll().toMap()

                withContext(Dispatchers.IO) {
                    githubDao.insertRepositories(repositories)
                    githubDao.insertReleases(releasesMap.values.filterNotNull())
                }

                _repositoryListState.value = repositories
                _releasesState.value = releasesMap
                _errorState.value = null

            } catch (e: Exception) {
                _errorState.value = "Couldn't refresh content. Displaying offline data."
                Log.e("CIT - GithubViewModel", "Unable to refresh data, showing cached content", e)
            }
        }
    }
}