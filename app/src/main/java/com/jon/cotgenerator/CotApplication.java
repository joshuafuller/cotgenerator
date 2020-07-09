package com.jon.cotgenerator;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.jon.cotgenerator.utils.Battery;
import com.jon.cotgenerator.utils.DeviceUid;

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
                return "(" + element.getFileName() + ":" + element.getLineNumber() + ")";
            }
        });

        /* Generate a device-specific UUID and save to file, if it doesn't already exist */
        DeviceUid.generate(this);

        /* Register a sticky intent to allow fetching battery status during runtime */
        Battery.getInstance().initialise(getContext());
    }
}
