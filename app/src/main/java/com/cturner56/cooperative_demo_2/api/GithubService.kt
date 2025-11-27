package com.cturner56.cooperative_demo_2.api

import com.cturner56.cooperative_demo_2.api.model.GithubRepository
import com.cturner56.cooperative_demo_2.api.model.RepositoryReleaseVersion
import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Purpose - a data class which models the JSON response from Github's API repository search endpoint.
 * Acting as a wrapper for the list of spotlighted repositories.
 * @param repos || A list of [GithubRepository] objects.
 * @return(s) A [RepositoriesResponse] object containing a list of retrieved repos.
 * doc-ref || "https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-repositories
 */
data class RepositoriesResponse(
    @Json(name = "repos")
    val repos: List<GithubRepository>
)

/**
 * Defines the Rest API endpoints for the Github API.
 */
interface GithubService {
    /**
     * Purpose - A function which retrieves details pertaining to a public Github repository.
     * @param owner || The username affiliated to the repo.
     * @param repo || The name of the repo.
     * @return(s) A [GithubRepository] object containing relevant repository details.
     * doc-ref || "https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#get-a-repository"
     */
    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
            // Requests without the X-GitHub-Api-Version header will default to use the 2022-11-28 version.
            // @Header("X-Github-Api-Version") apiVersion: String = "2022-11-28" // (Redundant to implement)
    ): GithubRepository

    /**
     * Purpose - A function which retrieves details pertaining to a repository's releases.
     * @param owner || The username affiliated to the repo.
     * @param repo || The name of the repo.
     * @return(s) A [List] of [RepositoryReleaseVersion] objects containing relevant release details.
     * doc-ref: https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28
     */
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<RepositoryReleaseVersion>

    /**
     * Purpose - A function to search for repositories.
     * @param query || The keywords used in searching.
     * @return(s) a list of repositories contained inside the [RepositoriesResponse] object.
     * doc-ref || https://docs.github.com/en/rest/search/search?apiVersion=2022-11-28#search-repositories
     */
    @GET("search/repositories")
    suspend fun queryRepositories(
        @Query("q") query: String
    ): RepositoriesResponse
}