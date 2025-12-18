package com.cturner56.streetwise_toolbox.api

import com.cturner56.streetwise_toolbox.api.model.GithubRepository
import com.cturner56.streetwise_toolbox.api.model.RepositoryReleaseVersion
import com.cturner56.streetwise_toolbox.db.GithubDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A manager class which is responsible for acting as the middle man between the Dao
 * and the Retrofit network api. It's purpose is to facilitate retrieval of data from the network
 * and, passes it to the ViewModel. Additionally, it is responsible for the caching of data into
 * the local room-db.
 */
class GithubRepositoryManager(private val dao: GithubDao) {
    private val api = Api.retrofitService //

    /**
     * A function which is responsible for retrieving all locally cached repositories which are
     * stored in the local Room-db.
     *
     * @return A list of cached [GithubRepository] objects.
     */
    suspend fun getCachedRepositories(): List<GithubRepository>
    = withContext(Dispatchers.IO) { dao.getAllRepositories() }

    /**
     * A function which is responsible for the retrieval of all releases associated with a specific
     * repository that are stored locally.
     *
     * @param repoFullName The full name ie 'owner/repo' of a repository.
     * @return A list of cached [RepositoryReleaseVersion] objects.
     */
    suspend fun getCachedReleases(repoFullName: String): List<RepositoryReleaseVersion>
    = withContext(Dispatchers.IO) { dao.getReleasesForRepository(repoFullName) }


    /**
     * A function which is responsible for inserting a list of repositories into the local Room-db.
     *
     * @param repositories The list of [GithubRepository] objects to insert locally.
     */
    suspend fun insertRepositories(repositories: List<GithubRepository>)
    = withContext(Dispatchers.IO) { dao.insertRepositories(repositories) }

    /**
     * A function which is responsible for inserting a list of repositories into the local Room-db.
     *
     * @param releases The list of [RepositoryReleaseVersion] objects to insert locally.
     */
    suspend fun insertReleases(releases: List<RepositoryReleaseVersion>)
    = withContext(Dispatchers.IO) { dao.insertReleases(releases) }

    /**
     * A function which is responsible for deleting a repository,
     * and it's corresponding release from the local Room-db.
     *
     * @param repository The [GithubRepository] object to delete.
     */
    suspend fun deleteRepository(repository: GithubRepository) = withContext(Dispatchers.IO) {
        dao.deleteReleasesForRepository(repository.fullName)
        dao.deleteRepository(repository)
    }

    /**
     * A function which is responsible for fetching a single repository directly from GitHub's API.
     *
     * @param owner The username associated with the repository owner.
     * @param repo The repository associated with the owner.
     */
    suspend fun fetchRepositoryFromNetwork(owner: String, repo: String): GithubRepository
    = withContext(Dispatchers.IO) { api.getRepository(owner, repo) }

    /**
     * A function which is responsible for fetching a release information directly from GitHub's API.
     *
     * @param owner The username associated with the repository owner.
     * @param repo The repository associated with the owner.
     */
    suspend fun fetchReleasesFromNetwork(owner: String, repo: String): List<RepositoryReleaseVersion>
    = withContext(Dispatchers.IO) { api.getReleases(owner, repo) }

    /**
     * A function which is responsible for retrieval, saving, and insertion of a new repository.
     * First it fetches repository details from the API, subsequently it does so for release information.
     * Lastly, it links the latest release to the repository and inserts such into the local instance.
     *
     * @param owner The username associated with the repository owner.
     * @param repoName The repository name associated with the owner.
     */
    suspend fun fetchAndSaveNewRepository(owner: String, repoName: String): GithubRepository =
        withContext(Dispatchers.IO) {
        val newlyInsertedRepo = api.getRepository(owner, repoName)
        val releases = api.getReleases(owner, repoName)
        val latestRelease = releases.firstOrNull()

        dao.insertRepository(newlyInsertedRepo)

        if (latestRelease != null) {
            latestRelease.repoFullName = newlyInsertedRepo.fullName
            dao.insertReleases(listOf(latestRelease))
        }

        return@withContext newlyInsertedRepo
    }

    /**
     * A function which is responsible for wiping local data when a user signs out.
     */
    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.deleteAllRepositories()
        dao.deleteAllReleases()
    }
}