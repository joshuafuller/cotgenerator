package com.jon.common;

import android.content.SharedPreferences;

import androidx.annotation.ColorRes;
import androidx.annotation.XmlRes;

import com.jon.common.service.CotFactory;
import com.jon.common.service.CotService;
import com.jon.common.ui.ListPresetsActivity;
import com.jon.common.ui.MainFragment;

import java.util.Date;

public class AppSpecific {
    AppSpecific() { /* blank */ }

    private static Repo repo = new DefaultRepo();

    public static void setReferenceRepo(Repo appSpecificRepo) {
        repo = appSpecificRepo;
    }

    public static MainFragment getMainFragment() { return repo.getMainFragment(); }
    public static CotFactory getCotFactory(SharedPreferences prefs) { return repo.getCotFactory(prefs); }
    public static Date getBuildDate() { return repo.getBuildDate(); }
    public static int getBuildVersionCode() { return repo.getBuildVersionCode(); }
    public static String getAppId() { return repo.getAppId(); }
    public static String getAppName() { return repo.getAppName(); }
    public static String getPermissionRationale() { return repo.getPermissionRationale(); }
    public static String getVersionName() { return repo.getVersionName(); }
    public static String getPlatform() { return repo.getPlatform(); }
    public static boolean isDebug() { return repo.isDebug(); }
    public static Class<? extends CotService> getCotServiceClass() { return repo.getCotServiceClass(); }
    public static Class<? extends ListPresetsActivity> getListActivityClass() { return repo.getListActivityClass(); }
    public static @XmlRes int getSettingsXmlId() { return repo.getSettingsXmlId(); }
    public static @ColorRes int getIconColourId() { return repo.getIconColourId(); }

    public interface Repo {
        MainFragment getMainFragment();
        CotFactory getCotFactory(SharedPreferences prefs);
        Date getBuildDate();
        int getBuildVersionCode();
        String getAppId();
        String getAppName();
        String getPermissionRationale();
        String getVersionName();
        String getPlatform();
        boolean isDebug();
        Class<? extends CotService> getCotServiceClass();
        Class<? extends ListPresetsActivity> getListActivityClass();
        @XmlRes int getSettingsXmlId();
        @ColorRes int getIconColourId();
    }
}
