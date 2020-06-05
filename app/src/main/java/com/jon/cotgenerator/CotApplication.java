package com.jon.cotgenerator;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class CotApplication extends Application {
    private static CotApplication instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        /* Set night mode */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        /* Initialise logging */
        Timber.plant(new Timber.DebugTree() {
            @Override protected String createStackElementTag(@NotNull StackTraceElement element) {
                return "(" + element.getFileName() + ":" + element.getLineNumber() + ")#" + element.getMethodName();
            }
        });
    }
}
