package com.cturner56.streetwise_toolbox.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cturner56.streetwise_toolbox.api.GithubRepositoryManager
import com.cturner56.streetwise_toolbox.api.UserRepositoryManager
import com.cturner56.streetwise_toolbox.api.model.GithubRepository
import com.cturner56.streetwise_toolbox.api.model.RepositoryReleaseVersion
import com.cturner56.streetwise_toolbox.db.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A [AndroidViewModel] which is responsible for retrieving and caching relevant repository information,
 * as well as release details for a pre-defined list of public Github repositories.
 *
 * Data flow is conducted between the UI and data layers. It fetches information from the local
 * Room-db via the [GithubRepositoryManager] to retrieve and update information.
 *
 * It exposes subsequently retrieved data, and or errors through the use of [State] objects.
 */
class GithubViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "CIT - GithubViewModel"

    private val repositoryManager: GithubRepositoryManager
    private val userRepositoryManager: UserRepositoryManager
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userSavedRepositories = emptyList<String>()

    /**
     * A listener which is responsible for triggering data fetching when the user auth state changes.
     * If logged in it'll sync user preferences and data. Otherwise, when logged out it will clear
     * user related data.
     */
    private  val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        // If the user is valid,
        // fetch their preferences and saved repositories utilizing UserRepositoryManager
        if (firebaseAuth.currentUser != null) {
            viewModelScope.launch {
                fetchNetworkRefreshPreference()
                fetchUserSavedRepos()
                fetchFeaturedRepos()
            }
        } else {
            viewModelScope.launch {
                // Resets memory state.
                _isNetworkRefreshDisabled.value = false
                userSavedRepositories = emptyList()

                // Clear local database cache.
                repositoryManager.clearAllData()

                // Clear the UI state
                _repositoryListState.value = emptyList()
                _releasesState.value = emptyMap()

                fetchFeaturedRepos() // Fetch default pre-defined repository list.
            }
        }
    }

    /**
     * Responsible for initializing the database Dao and [GithubRepositoryManager].
     * Once initialized, it then attaches the auth state listener.
     */
    init {
        val dao = AppDatabase.getInstance(application).githubDao()
        repositoryManager = GithubRepositoryManager(dao)
        userRepositoryManager = UserRepositoryManager()
        auth.addAuthStateListener (authStateListener)
    }

    /**
     * Purges the auth state listener when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

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

    // Holds the state for toggling network refresh of repository data.
    private val _isNetworkRefreshDisabled = mutableStateOf(false)
    val isNetworkRefreshDisabled: State<Boolean> = _isNetworkRefreshDisabled

    /**
     * A private function which requests the 'disableNetworkRefresh' bool from a user's
     * Firestore document.
     */
    private suspend fun fetchNetworkRefreshPreference() {
        val isDisabled = userRepositoryManager.getNetworkRefreshPreference()
        _isNetworkRefreshDisabled.value = isDisabled
        Log.d(TAG, "Loaded network preferences from the cloud: Disabled=$isDisabled")
    }


    /**
     * A private function which is responsible for fetching user-saved repositories.
     */
    private suspend fun fetchUserSavedRepos() {
        userSavedRepositories = userRepositoryManager.getUserSavedRepoIds()
        Log.d(TAG, "Loaded ${userSavedRepositories.size} custom repositories from Firestore.")
    }

    /**
     * A function which is responsible for toggling the network preference locally,
     * and saving the user preference to Firestore.
     */
    fun toggleNetworkRefresh(isDisabled: Boolean) {
        _isNetworkRefreshDisabled.value = isDisabled // Update UI so switch immediately responds.

        viewModelScope.launch {
            userRepositoryManager.updateNetworkRefreshPreferences(isDisabled)
        }

        if (!isDisabled) {
            fetchFeaturedRepos()
        }
    }

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
     */
    fun fetchFeaturedRepos() {
        viewModelScope.launch {
            loadFromCache()

            val isCacheEmpty = _repositoryListState.value.isEmpty()

            if (!_isNetworkRefreshDisabled.value || isCacheEmpty) {

                if (isCacheEmpty && _isNetworkRefreshDisabled.value) {
                    Log.i(TAG, "User disabled network refresh but cache is empty, forcing initial fetch.")
                }

                val wasRefreshSuccessful = refreshFromNetwork()

                if (!wasRefreshSuccessful) {
                    _errorState.value = "[OFFLINE]\nDisplaying Local Cache"
                }
            } else {
                Log.d(TAG, "Repository refresh disabled per user preference. Displaying offline data.")
            }
        }
    }

    /**
     * A function which is responsible for coordinating the addition of a repository. Using
     * [GithubRepositoryManager] to insert into local cache and [UserRepositoryManager] to also add
     * such to the user's remotely saved list.
     */
    fun addRepository(owner: String, repoName: String) {
        viewModelScope.launch {
            try {
                val newRepo = repositoryManager.fetchAndSaveNewRepository(owner, repoName)

                // Delegates Firestore sync to UserRepositoryManager.
                userRepositoryManager.addSavedRepo(newRepo.fullName)
                userSavedRepositories = userSavedRepositories + newRepo.fullName

                Log.d(TAG, "${newRepo.fullName} and release info added to the database.")
                fetchFeaturedRepos() // Refreshes UI
                _errorState.value = null // Clear previous errors.
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add $owner/$repoName", e)
                _errorState.value = "Couldn't fetch information pertaining to $owner/$repoName."
            }
        }
    }

    /**
     * A function which is responsible for coordinating the deletion of a repository. Using
     * [GithubRepositoryManager] to purge local cache and [UserRepositoryManager] to remove such
     * from the user's remotely saved list.
     */
    fun deleteRepository(repo: GithubRepository) {
        viewModelScope.launch {
            try {
                repositoryManager.deleteRepository(repo)

                userRepositoryManager.removeSavedRepo(repo.fullName)

                Log.d(TAG , "${repo.fullName} " + "Purged from database.")
                loadFromCache() // Re-run fetch logic to update UI
                _errorState.value = null // Clear previous errors.
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove ${repo.fullName}", e)
                _errorState.value = "Couldn't remove information pertaining to ${repo.fullName}."
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
    private suspend fun loadFromCache() {
        try {
            val (cachedRepositories, cachedReleasesMap) = withContext(Dispatchers.IO) {
                val repos = repositoryManager.getCachedRepositories()
                val releases = repos.associate { repo ->
                    repo.fullName to repositoryManager.getCachedReleases(repo.fullName).firstOrNull()
                }
                repos to releases
            }
            if (cachedRepositories.isNotEmpty()) {
                Log.d(TAG, "Successfully loaded repositories from cache.")
                _repositoryListState.value = cachedRepositories
                _releasesState.value = cachedReleasesMap
            } else {
                Log.d(TAG, "Local instance is empty. Attempting to fetch via network.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load repository information from offline cache.", e)
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
    private suspend fun refreshFromNetwork(): Boolean {
        return try {
            _errorState.value = null // Clears any existing error messages.

            var refreshedRepositories = withContext(Dispatchers.IO) {
                repositoryManager.getCachedRepositories().map {it.fullName}
            }

            if (refreshedRepositories.isEmpty()) {
                refreshedRepositories = (featuredRepositories + userSavedRepositories).distinct()
            }

            // Creates a list of async jobs for each repository.
            val repositories = withContext(Dispatchers.IO) {
                val repositoryRetrieval = refreshedRepositories.map { fullName ->
                    async {
                        try {
                            val (owner, repo) = fullName.split("/")
                            repositoryManager.fetchRepositoryFromNetwork(owner, repo)
                        } catch (e: Exception) {
                        Log.e(TAG, "Failed to refresh repository from online resource: $fullName", e)
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
                            val latestRelease = repositoryManager
                                .fetchReleasesFromNetwork(owner, name)
                                .firstOrNull()
                            latestRelease?.repoFullName = repo.fullName
                            repo.fullName to latestRelease
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to fetch releases for ${repo.fullName}", e)
                            repo.fullName to null
                        }
                    }
                }
                releaseRetrieval.awaitAll().toMap()
            }
            _repositoryListState.value = repositories // Update UI State
            _releasesState.value = releasesMap // Updates UI State

            withContext(Dispatchers.IO) { // Writes newly retrieved data to local Room -db instance.
                repositoryManager.insertRepositories(repositories)
                // Ensures null releases aren't inserted into the database.
                repositoryManager.insertReleases(releasesMap.values.filterNotNull())
                Log.d(
                    TAG, "Information successfully retrieved from online sources, " +
                    "updated local cache with ${repositories.size} repos!"
                )
            }
            true
        } catch (e: Exception) {
                Log.e(TAG, "Unable to refresh data, showing cached content", e)
            false
        }
    }
}