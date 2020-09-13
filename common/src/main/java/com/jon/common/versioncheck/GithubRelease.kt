package com.jon.common.versioncheck;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

data class GithubRelease(
        @Expose
        @SerializedName("name")
        val name: String,

        @SerializedName("published_at")
        @Expose
        val publishedAt: String
)
