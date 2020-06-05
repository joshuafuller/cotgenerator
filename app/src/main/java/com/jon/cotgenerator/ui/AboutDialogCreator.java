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
import com.jon.cotgenerator.updating.Release;
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
    private static final List<Row> ROWS = Arrays.asList(
            new Row("Build Time", getBuildDate(), null),
            new Row("Build Type", BuildConfig.DEBUG ? "Debug" : "Release", null),
            new Row("Installed Version", BuildConfig.VERSION_NAME, null),
            new Row("Latest Github Release", LOADING, R.drawable.refresh),
            new Row("Github Repository", "https://github.com/jonapoul/cotgenerator", R.drawable.go_to)
    );
    private static final int LATEST_INDEX = 3;
    private static final int GITHUB_INDEX = 4;
    private static final Callback<List<Release>> callback = new Callback<List<Release>>() {
        @Override public void onResponse(@NonNull Call<List<Release>> call, @NonNull Response<List<Release>> response) {
            Release latest = UpdateChecker.getLatestRelease(response.body());
            if (latest != null) {
                String suffix = getVersionSuffix(latest.getName());
                ROWS.get(LATEST_INDEX).subtitle = latest.getName() + suffix;
                adapter.notifyDataSetChanged();
            } else {
                onFailure(call, new HttpException(response));
            }
        }
        @Override public void onFailure(@NonNull Call<List<Release>> call, @NonNull Throwable t) {
            ROWS.get(LATEST_INDEX).subtitle = "[Error: " + t.getMessage() + "]";
            adapter.notifyDataSetChanged();
        }
    };

    private static ArrayAdapter<Row> adapter;

    private AboutDialogCreator() {
    }

    static void show(Context context) {
        ROWS.get(LATEST_INDEX).subtitle = LOADING;
        adapter = new ArrayAdapter<Row>(context, R.layout.about_listview_item, R.id.aboutItemTextTitle, ROWS) {
            @NonNull @Override public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, null, parent);
                Row row = ROWS.get(position);
                ((TextView) view.findViewById(R.id.aboutItemTextTitle)).setText(row.title);
                ((TextView) view.findViewById(R.id.aboutItemTextSubtitle)).setText(row.subtitle);
                ImageView button = view.findViewById(R.id.aboutItemIcon);
                if (row.icon != null) {
                    button.setImageDrawable(ContextCompat.getDrawable(context, row.icon));
                }
                return view;
            }
        };
        ListView listView = (ListView) View.inflate(context, R.layout.about_listview, null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == GITHUB_INDEX) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(ROWS.get(position).subtitle));
                context.startActivity(intent);
            } else if (position == LATEST_INDEX) {
                ROWS.get(LATEST_INDEX).subtitle = LOADING;
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

    private static String getVersionSuffix(String discoveredVersionStr) {
        try {
            int installed = BuildConfig.VERSION_CODE;
            int discovered = Integer.parseInt(discoveredVersionStr.replaceAll("\\.", "")); // "1.3.2" -> 132
            if (discovered > installed) {
                return " - Update available!";
            } else if (installed == discovered) {
                return " - You're up-to-date!";
            } else { // installed > discovered
                return " - You're from the future!";
            }
        } catch (NumberFormatException e) {
            return " - Failed parsing version numbers! Tell me if you see this please...";
        }
    }

    private static class Row {
        String title; String subtitle; Integer icon;
        Row(String title, String subtitle, Integer icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }
    }
}
