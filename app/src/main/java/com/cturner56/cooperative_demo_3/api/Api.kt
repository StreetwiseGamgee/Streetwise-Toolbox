package com.cturner56.cooperative_demo_3.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Builds & provides the Retrofit client for Github's RestAPI.
 * It is responsible for any / and all configs required by Retrofit.
 * Imp-Ref: Based on implementation provided by MovieHubFall2025.
 */
object Api {
    private val BASE_URL = "https://api.github.com" // Base url for all Github Rest API endpoints

    /**
     * An instance of Moshi which is used by Retrofit to parse JSON responses into Kotlin Obj's
     * Configured with [KotlinJsonAdapterFactory] to support language specific features.
     */
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Responsible for the creation, and execution of network requests.
     */
    private val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .baseUrl(BASE_URL)
        .build()

    val retrofitService: GithubService by lazy {
        retrofit.create(GithubService::class.java)
    }
}