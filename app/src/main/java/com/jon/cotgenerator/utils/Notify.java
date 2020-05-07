package com.jon.cotgenerator.utils;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.jon.cotgenerator.R;

public final class Notify {
    private Notify() {
    }

    private static void snackbar(View root, String str, int backgroundColour, int textColour, View.OnClickListener action, String actionMsg) {
        Snackbar snackbar = Snackbar.make(root, str, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(backgroundColour);
        TextView text = view.findViewById(com.google.android.material.R.id.snackbar_text);
        text.setTextColor(textColour);
        if (action != null && actionMsg != null) {
            snackbar.setAction(actionMsg, action);
            snackbar.setActionTextColor(textColour);
        }
        snackbar.show();
    }

    public static void red(View root, String str, View.OnClickListener action, String actionMsg) {
        snackbar(root, str, Color.RED, Color.BLACK, action, actionMsg);
    }

    public static void green(View root, String str) {
        snackbar(root, str, Color.GREEN, Color.BLACK, null, null);
    }

    public static void red(View root, String str) {
        red(root, str, null, null);
    }

    public static void orange(View root, String str) {
        snackbar(root, str, Color.parseColor("#FFA600"), Color.BLACK, null, null);
    }
}

