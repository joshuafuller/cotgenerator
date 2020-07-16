package com.jon.common;

import android.content.SharedPreferences;

import com.jon.common.service.CotFactory;
import com.jon.common.service.CotService;
import com.jon.common.ui.ListPresetsActivity;
import com.jon.common.ui.MainFragment;

import java.util.Date;

/* App-specific repo used for unit testing, since we don't have an Application class to set this value */
public class DefaultRepo implements AppSpecific.Repo {
    @Override
    public MainFragment getMainFragment() {
        return null;
    }

    @Override
    public CotFactory getCotFactory(SharedPreferences prefs) {
        return null;
    }

    @Override
    public Date getBuildDate() {
        return null;
    }

    @Override
    public int getBuildVersionCode() {
        return 0;
    }

    @Override
    public String getAppId() {
        return null;
    }

    @Override
    public String getAppName() {
        return null;
    }

    @Override
    public String getPermissionRationale() {
        return null;
    }

    @Override
    public String getVersionName() {
        return "VERSION-NAME";
    }

    @Override
    public String getPlatform() {
        return "PLATFORM";
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public Class<? extends CotService> getCotServiceClass() {
        return null;
    }

    @Override
    public Class<? extends ListPresetsActivity> getListActivityClass() {
        return null;
    }

    @Override
    public int getSettingsXmlId() {
        return 0;
    }

    @Override
    public int getIconColourId() {
        return 0;
    }
}
