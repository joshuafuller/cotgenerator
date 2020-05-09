package com.jon.cotgenerator.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jon.cotgenerator.BuildConfig;
import com.jon.cotgenerator.R;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

class AboutDialogCreator {
    private AboutDialogCreator() {
    }

    static void show(Context context) {
        List<String> titles = Arrays.asList("Version", "Build Time", "Github Repository");
        List<String> items = Arrays.asList(
                BuildConfig.VERSION_NAME,
                BuildConfig.BUILD_TIME.toInstant().atZone(ZoneId.of("UTC")).format(DateTimeFormatter.ofPattern("HH:mm:ss dd MMM YYYY z")),
                "https://github.com/jonapoul/cotgenerator"
        );
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_2, android.R.id.text1, items) {
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                text1.setText(titles.get(position));
                text2.setText(items.get(position));
                return view;
            }
        };
        ListView listView = (ListView) View.inflate(context, R.layout.about_listview, null);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 2) { // github repo URL
                /* Open the URL in the browser */
                String url = items.get(position);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
            }
        });
        new MaterialAlertDialogBuilder(context)
                .setTitle("About")
                .setView(listView)
                .setPositiveButton(android.R.string.ok, (dialog, buttonId) -> dialog.dismiss())
                .show();
    }
}
