package com.jon.common.versioncheck

import retrofit2.Call
import retrofit2.http.GET

interface GithubApi {
    @GET("/repos/jonapoul/cotgenerator/releases")
    fun getAllReleases(): Call<List<GithubRelease>>
}
