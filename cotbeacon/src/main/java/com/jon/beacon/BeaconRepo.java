package com.jon.beacon;

import com.jon.common.AppSpecific;
import com.jon.common.service.CotFactory;
import com.jon.common.service.CotService;
import com.jon.common.ui.ListPresetsActivity;
import com.jon.common.ui.MainFragment;

import java.util.Date;

public class BeaconRepo extends AppSpecific.Repo {

    @Override
    protected MainFragment getMainFragment() {
        return BeaconFragment.getInstance();
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
        return BeaconService.class;
    }

    @Override
    protected Class<? extends CotFactory> getCotFactoryClass() {
        return BeaconCotFactory.class;
    }

    @Override
    protected Class<? extends ListPresetsActivity> getListActivityClass() {
        return BeaconListPresetsActivity.class;
    }

    @Override
    public int getSettingsXmlId() {
        return R.xml.settings;
    }

    @Override
    protected int getIconColourId() {
        return R.color.black;
    }
}
