package com.cturner56.streetwise_toolbox.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cturner56.streetwise_toolbox.api.model.GithubRepository
import com.cturner56.streetwise_toolbox.api.model.RepositoryReleaseVersion

/**
 * DAO - For Github Repository Related Data
 *
 * The interface defines db operations for 'github_repos' and 'github_releases' tables.
 * Allowing the GithubRepositoryManager to perform insert, delete and query operations on cached data.
 * fetched from the Restful API. [GithubRepository] and [RepositoryReleaseVersion] 's data classes
 * are used in storing, updating, and querying data.
 */
@Dao
interface GithubDao {
    /**
     * Inserts a single [GithubRepository] into the 'github_repos table.
     *
     * @param repo the [GithubRepository] object to be inserted or replaced (Should it exist).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repo: GithubRepository)

    /**
     * Inserts a list of [GithubRepository] objects into the 'github_repos' table.
     *
     * @param repos A list of [GithubRepository] objects which are to be inserted, and or replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepositories(repos: List<GithubRepository>)

    /**
     * Retrieves all cached [GithubRepository] objects stored in the 'github_repos' table.
     *
     * @return A list of all [GithubRepository] objects stored currently in the database.
     */
    @Query("SELECT * FROM github_repos")
    suspend fun getAllRepositories(): List<GithubRepository>

    /**
     * Inserts a list of [RepositoryReleaseVersion] objects into the 'github_releases' table.
     *
     * @param releases A list of [RepositoryReleaseVersion] objects which are to be inserted,
     * and or replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReleases(releases: List<RepositoryReleaseVersion>)

    /**
     * Retrieves all cached [RepositoryReleaseVersion] objects stored in the 'github_releases' table.
     *
     * @return A list of all [RepositoryReleaseVersion] objects stored currently in the database.
     */
    @Query("SELECT * FROM github_releases WHERE repoFullName = :repoFullName")
    suspend fun getReleasesForRepository(repoFullName: String): List<RepositoryReleaseVersion>

    /**
     * Deletes a single instance of a GitHub repository from the 'github_repos' table.
     *
     * @param repo the [GithubRepository] object which is to be deleted.
     */
    @Delete
    suspend fun deleteRepository(repo: GithubRepository)

    /**
     * Deletes a single instance of a GitHub release from the 'github_releases' table.
     *
     * @param repoFullName the full name (owner/repo) that is associated with a repository
     */
    @Query("DELETE FROM github_releases WHERE repoFullName = :repoFullName")
    suspend fun deleteReleasesForRepository(repoFullName: String)
}