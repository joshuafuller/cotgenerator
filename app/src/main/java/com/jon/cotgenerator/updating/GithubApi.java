package com.jon.cotgenerator.updating;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GithubApi {
    @GET("/repos/jonapoul/cotgenerator/releases")
    Call<List<JsonRelease>> getAllReleases();
}
