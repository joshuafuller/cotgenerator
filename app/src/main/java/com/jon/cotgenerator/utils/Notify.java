package com.jon.cotgenerator.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public final class Notify {
    private Notify() {
    }

    private static void snackbar(View root, String str, int backgroundColour, int textColour) {
        Snackbar snackbar = Snackbar.make(root, str, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(backgroundColour);
        TextView text = view.findViewById(com.google.android.material.R.id.snackbar_text);
        text.setTextColor(textColour);
        snackbar.show();
    }

    public static void green(View root, String str) {
        snackbar(root, str, Color.GREEN, Color.BLACK);
    }

    public static void red(View root, String str) {
        snackbar(root, str, Color.RED, Color.BLACK);
    }

    public static void blue(View root, String str) {
        snackbar(root, str, Color.BLUE, Color.WHITE);
    }

    public static void orange(View root, String str) {
        snackbar(root, str, Color.parseColor("#FFA600"), Color.BLACK);
    }
}

