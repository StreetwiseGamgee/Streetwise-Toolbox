package com.cturner56.cooperative_demo_2.api

import com.cturner56.cooperative_demo_2.api.model.GithubRepository
import com.cturner56.cooperative_demo_2.api.model.RepositoryReleaseVersion
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Defines the Rest API endpoints for the Github API.
 */
interface GithubService {
    /**
     * Purpose - A function which retrieves details pertaining to a public Github repository.
     * @param owner || The username affiliated to the repo.
     * @param repo || The name of the repo.
     * @return a [GithubRepository] object containing relevant repo details.
     * doc-ref (loose ref) || "https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#get-a-repository"
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
     * @return a [List] of [RepositoryReleaseVersion] objects containing relevant release details.
     */
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<RepositoryReleaseVersion>
}