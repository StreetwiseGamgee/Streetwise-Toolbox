package com.cturner56.streetwise_toolbox.api

import com.cturner56.streetwise_toolbox.api.model.GithubRepository
import com.cturner56.streetwise_toolbox.api.model.RepositoryReleaseVersion
import com.cturner56.streetwise_toolbox.db.GithubDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A manager class which is responsible for acting as the middle man between tha Dao
 * and the Retrofit network api. It's purpose is to facilitate retrieval of data from the network
 * and, passes it to the ViewModel. Additionally, it is responsible for the caching of data into
 * the local room-db.
 */
class GithubRepositoryManager(private val dao: GithubDao) {
    private val api = Api.retrofitService

    suspend fun getCachedRepositories(): List<GithubRepository>
    = withContext(Dispatchers.IO) { dao.getAllRepositories() }

    suspend fun getCachedReleases(repoFullName: String): List<RepositoryReleaseVersion>
    = withContext(Dispatchers.IO) { dao.getReleasesForRepository(repoFullName) }

    suspend fun insertRepository(repository: GithubRepository): List<GithubRepository>
    = withContext(Dispatchers.IO) {
        dao.insertRepository(repository)
        dao.getAllRepositories()
    }

    suspend fun insertRepositories(repositories: List<GithubRepository>)
    = withContext(Dispatchers.IO) { dao.insertRepositories(repositories) }

    suspend fun insertReleases(releases: List<RepositoryReleaseVersion>)
    = withContext(Dispatchers.IO) { dao.insertReleases(releases) }

    suspend fun deleteRepository(repository: GithubRepository) = withContext(Dispatchers.IO) {
        dao.deleteReleasesForRepository(repository.fullName)
        dao.deleteRepository(repository)
    }

    suspend fun fetchRepositoryFromNetwork(owner: String, repo: String): GithubRepository
    = withContext(Dispatchers.IO) { api.getRepository(owner, repo) }

    suspend fun fetchReleasesFromNetwork(owner: String, repo: String): List<RepositoryReleaseVersion>
    = withContext(Dispatchers.IO) { api.getReleases(owner, repo) }
}