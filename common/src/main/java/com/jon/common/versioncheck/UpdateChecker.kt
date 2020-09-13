package com.jon.common.versioncheck

import com.jon.common.cot.UtcTimestamp
import retrofit2.Callback

class UpdateChecker {
    fun check(callback: Callback<List<GithubRelease>>) {
        val githubApi = RetrofitClient.get().create(GithubApi::class.java)
        githubApi.getAllReleases().enqueue(callback)
    }

    fun getLatestRelease(releases: List<GithubRelease>?): GithubRelease? {
        return releases?.maxByOrNull { UtcTimestamp(it.publishedAt).milliseconds() }
    }
}
