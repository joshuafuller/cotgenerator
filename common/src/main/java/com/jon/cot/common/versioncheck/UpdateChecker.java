package com.jon.cot.common.versioncheck;

import androidx.annotation.Nullable;

import com.jon.cot.common.cot.UtcTimestamp;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;

public class UpdateChecker {
    private UpdateChecker() { }

    public static void check(Callback<List<Release>> callback) {
        GithubApi updateChecker = RetrofitClient.get().create(GithubApi.class);
        Call<List<Release>> call = updateChecker.getAllReleases();
        call.enqueue(callback);
    }

    @Nullable
    public static Release getLatestRelease(List<Release> releases) {
        if (releases == null) return null;
        Release latestRelease = null;
        long latestPublishDate = 0;
        for (Release release : releases) {
            UtcTimestamp publishDate = new UtcTimestamp(release.getPublishedAt());
            if (publishDate.toLong() > latestPublishDate) {
                latestPublishDate = publishDate.toLong();
                latestRelease = release;
            }
        }
        return latestRelease;
    }
}
