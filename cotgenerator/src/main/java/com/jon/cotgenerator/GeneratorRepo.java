package com.jon.cotgenerator;

import com.jon.common.AppSpecific;
import com.jon.common.service.CotFactory;
import com.jon.common.service.CotService;
import com.jon.common.ui.ListPresetsActivity;
import com.jon.common.ui.MainFragment;

import java.util.Date;

public class GeneratorRepo extends AppSpecific.Repo {

    @Override
    protected MainFragment getMainFragment() {
        return GeneratorFragment.getInstance();
    }

    @Override
    protected Date getBuildDate() {
        return BuildConfig.BUILD_TIME;
    }

    @Override
    protected int getBuildVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    protected String getAppId() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    protected String getAppName() {
        return context().getString(R.string.app_name);
    }

    @Override
    protected String getPermissionRationale() {
        return context().getString(R.string.permissionRationale);
    }

    @Override
    protected String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    protected String getPlatform() {
        return context().getString(R.string.appNameAllCaps);
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected Class<? extends CotService> getCotServiceClass() {
        return GeneratorService.class;
    }

    @Override
    protected Class<? extends CotFactory> getCotFactoryClass() {
        return GeneratorCotFactory.class;
    }

    @Override
    protected Class<? extends ListPresetsActivity> getListActivityClass() {
        return ListPresetsActivity.class;
    }

    @Override
    public int getSettingsXmlId() {
        return R.xml.settings;
    }

    @Override
    protected int getIconColourId() {
        return R.color.white;
    }
}
