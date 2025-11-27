package com.cturner56.cooperative_demo_2.viewmodel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cturner56.cooperative_demo_2.api.Api
import com.cturner56.cooperative_demo_2.api.model.GithubRepository
import com.cturner56.cooperative_demo_2.api.model.RepositoryReleaseVersion
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlin.collections.forEach
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

/**
 * Purpose - A ViewModel which exposes relevant repo information, and
 * release details for a public Github repository.
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
     * Purpose - Fetches a predefined list of featured repositories.
     * The function launches coroutines to fetch both repository information and release details.
     * for each repo listed inside [featuredRepositories].
     */
    fun fetchFeaturedRepos() {
        viewModelScope.launch {
            try {
                // Clears previous data, initiates loading.
                _repositoryListState.value = emptyList()
                _releasesState.value = emptyMap()
                _errorState.value = null // Preventing previous errors from displaying

                // fetch repository information
                val repositoryRetrieval = featuredRepositories.map { fullName ->
                    async {
                        val (owner, repo) = fullName.split("/")
                        Api.retrofitService.getRepository(owner, repo)
                    }
                }
                val repositories = repositoryRetrieval.awaitAll()
                _repositoryListState.value = repositories

                // Fetch release information
                val releaseRetrieval = repositories.map { repo ->
                    async {
                        val (owner, name) = repo.fullName.split("/")
                        try{
                            val releases = Api.retrofitService.getReleases(owner, name)
                            // On success, returns a pair containing the name and release
                            repo.fullName to releases.firstOrNull()
                        } catch (e: HttpException) {
                            Log.w("CIT - GithubViewModel", "No releases available for ${repo.fullName}", e)
                            // On failure, returns a pair of the name and null.
                            repo.fullName to null
                        }
                    }
                }
                _releasesState.value = releaseRetrieval.awaitAll().toMap()

            } catch (e: HttpException) {
                //
                _errorState.value = "Repository retrieval failed, please refresh application."
                Log.e("CIT - GithubViewModel", "An API Error has occurred, ${e.message}")
            } catch (e: IOException) {
                // Handles network error output.
                _errorState.value = "Network error has occurred, please connect to the internet"
                Log.e("CIT - GithubViewModel", "Network error has occurred, ${e.message}")
            } catch (e: Exception) {
                //
                _errorState.value = "Sorry, we've hit a wall. Another error has occurred ${e.message}"
                Log.e("CIT - GithubViewModel", "An unexpected error has occurred ${e.message}")
            }
        }
    }
}