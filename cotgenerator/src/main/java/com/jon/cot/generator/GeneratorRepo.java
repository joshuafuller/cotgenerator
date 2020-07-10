package com.jon.cot.generator;

import com.jon.cot.common.AppSpecific;
import com.jon.cot.common.service.CotFactory;
import com.jon.cot.common.service.CotService;
import com.jon.cot.common.ui.MainFragment;
import com.jon.cot.generator.service.GeneratorCotFactory;
import com.jon.cot.generator.service.GeneratorService;
import com.jon.cot.generator.ui.GeneratorFragment;

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
    public int getSettingsXmlId() {
        return R.xml.settings;
    }

}
