package com.jon.common.versioncheck

import io.reactivex.Observable
import retrofit2.http.GET

interface GithubApi {
    @GET("/repos/jonapoul/cotgenerator/releases")
    fun getAllReleases(): Observable<List<GithubRelease>>
}
