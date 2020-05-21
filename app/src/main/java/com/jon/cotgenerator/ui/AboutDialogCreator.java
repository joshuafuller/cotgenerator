package com.jon.cotgenerator.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.updating.JsonRelease;
import com.jon.cotgenerator.updating.UpdateChecker;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

class AboutDialogCreator {
    private static final String LOADING = "Loading...";
    private static final List<Line> DATA = Arrays.asList(
            new Line("Build Time", getBuildDate(), null),
            new Line("Installed Version", BuildConfig.VERSION_NAME, null),
            new Line("Latest Version", LOADING, R.drawable.refresh),
            new Line("Github Repository", "https://github.com/jonapoul/cotgenerator", R.drawable.go_to)
    );
    private static final int LATEST_INDEX = 2;
    private static final int GITHUB_INDEX = 3;
    private static final Callback<List<JsonRelease>> callback = new Callback<List<JsonRelease>>() {
        @Override public void onResponse(@NonNull Call<List<JsonRelease>> call, @NonNull Response<List<JsonRelease>> response) {
            JsonRelease latest = UpdateChecker.getLatestRelease(response.body());
            if (latest != null) {
                boolean isNew = !latest.getName().equals(BuildConfig.VERSION_NAME);
                DATA.get(LATEST_INDEX).subtitle = latest.getName() + (isNew ? " - Update available!" : " - You're up-to-date!");
                adapter.notifyDataSetChanged();
            } else {
                onFailure(call, new HttpException(response));
            }
        }
        @Override public void onFailure(@NonNull Call<List<JsonRelease>> call, @NonNull Throwable t) {
            DATA.get(LATEST_INDEX).subtitle = "[Error: " + t.getMessage() + "]";
            adapter.notifyDataSetChanged();
        }
    };

    private static ArrayAdapter<Line> adapter;

    private AboutDialogCreator() {
    }

    static void show(Context context) {
        DATA.get(LATEST_INDEX).subtitle = LOADING;
        adapter = new ArrayAdapter<Line>(context, R.layout.about_listview_item, R.id.aboutItemTextTitle, DATA) {
            @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, null, parent);
                Line line = DATA.get(position);
                ((TextView) view.findViewById(R.id.aboutItemTextTitle)).setText(line.title);
                ((TextView) view.findViewById(R.id.aboutItemTextSubtitle)).setText(line.subtitle);
                ImageView button = view.findViewById(R.id.aboutItemIcon);
                if (line.icon != null) {
                    button.setImageDrawable(ContextCompat.getDrawable(context, line.icon));
                }
                return view;
            }
        };
        ListView listView = (ListView) View.inflate(context, R.layout.about_listview, null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == GITHUB_INDEX) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(DATA.get(position).subtitle));
                context.startActivity(intent);
            } else if (position == LATEST_INDEX) {
                DATA.get(LATEST_INDEX).subtitle = LOADING;
                adapter.notifyDataSetChanged();
                UpdateChecker.check(callback);
            }
        });
        new MaterialAlertDialogBuilder(context)
                .setTitle("About")
                .setView(listView)
                .setPositiveButton(android.R.string.ok, (dialog, buttonId) -> dialog.dismiss())
                .show();
        UpdateChecker.check(callback);
    }

    private static String getBuildDate() {
        return new SimpleDateFormat("HH:mm:ss dd MMM yyyy z", Locale.ENGLISH).format(BuildConfig.BUILD_TIME);
    }

    private static class Line {
        String title; String subtitle; Integer icon;
        Line(String title, String subtitle, Integer icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }
    }
}
