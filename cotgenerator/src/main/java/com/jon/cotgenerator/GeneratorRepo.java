package com.jon.cotgenerator;

import android.content.SharedPreferences;

import com.jon.common.AppSpecific;
import com.jon.common.CotApplication;
import com.jon.common.service.CotFactory;
import com.jon.common.service.CotService;
import com.jon.common.ui.ListPresetsActivity;
import com.jon.common.ui.MainFragment;

import java.util.Date;

public class GeneratorRepo implements AppSpecific.Repo {

    @Override
    public MainFragment getMainFragment() {
        return GeneratorFragment.getInstance();
    }

    @Override
    public CotFactory getCotFactory(SharedPreferences prefs) {
        return new GeneratorCotFactory(prefs);
    }

    @Override
    public Date getBuildDate() {
        return BuildConfig.BUILD_TIME;
    }

    @Override
    public int getBuildVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public String getAppId() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    public String getAppName() {
        return CotApplication.getContext().getString(R.string.app_name);
    }

    @Override
    public String getPermissionRationale() {
        return CotApplication.getContext().getString(R.string.permissionRationale);
    }

    @Override
    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public String getPlatform() {
        return CotApplication.getContext().getString(R.string.appNameAllCaps);
    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public Class<? extends CotService> getCotServiceClass() {
        return GeneratorService.class;
    }

    @Override
    public Class<? extends ListPresetsActivity> getListActivityClass() {
        return ListPresetsActivity.class;
    }

    @Override
    public int getSettingsXmlId() {
        return R.xml.settings;
    }

    @Override
    public int getIconColourId() {
        return R.color.white;
    }
}
