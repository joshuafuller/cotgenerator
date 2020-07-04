package com.jon.cotgenerator.versioncheck;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GithubApi {
    @GET("/repos/jonapoul/cotgenerator/releases")
    Call<List<Release>> getAllReleases();
}
