package com.jon.cotgenerator;

import android.app.Application;
import android.content.Context;

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
        Timber.plant(new Timber.DebugTree() {
            @Override protected String createStackElementTag(@NotNull StackTraceElement element) {
                return "(" + element.getFileName() + ":" + element.getLineNumber() + ")#" + element.getMethodName();
            }
        });
    }
}
