package com.jon.cotgenerator.updating;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Release {
    @SerializedName("name") @Expose private String name;
    @SerializedName("published_at") @Expose private String publishedAt;

    public String getName() {
        return name;
    }
    public String getPublishedAt() {
        return publishedAt;
    }
}
