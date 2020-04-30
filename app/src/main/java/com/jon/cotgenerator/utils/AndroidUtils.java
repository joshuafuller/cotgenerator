package com.jon.cotgenerator.utils;

import android.content.Context;
import android.widget.Toast;

public final class AndroidUtils {
    private AndroidUtils() { }

    public static void toast(final Context context, final String str) {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
}

