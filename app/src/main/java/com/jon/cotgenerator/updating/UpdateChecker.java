package com.jon.cotgenerator.updating;

import androidx.annotation.Nullable;

import com.jon.cotgenerator.cot.UtcTimestamp;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class UpdateChecker {
    private UpdateChecker() { }

    public static void check(Callback<List<JsonRelease>> callback) {
        GithubApi updateChecker = RetrofitClient.get().create(GithubApi.class);
        Call<List<JsonRelease>> call = updateChecker.getAllReleases();
        call.enqueue(callback);
    }

    @Nullable
    public static JsonRelease getLatestRelease(List<JsonRelease> releases) {
        if (releases == null) return null;
        JsonRelease latestRelease = null;
        long latestPublishDate = Long.MAX_VALUE;
        for (JsonRelease release : releases) {
            UtcTimestamp publishDate = new UtcTimestamp(release.getPublishedAt());
            if (publishDate.toLong() < latestPublishDate) {
                latestPublishDate = publishDate.toLong();
                latestRelease = release;
            }
        }
        return latestRelease;
    }
}
