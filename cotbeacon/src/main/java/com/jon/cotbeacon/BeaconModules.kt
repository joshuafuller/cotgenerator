package com.jon.cotbeacon

import android.content.Context
import android.content.SharedPreferences
import com.jon.common.di.ActivityResources
import com.jon.common.di.BuildResources
import com.jon.common.repositories.IBatteryRepository
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.IGpsRepository
import com.jon.common.service.CotFactory
import com.jon.common.ui.main.SettingsFragment
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
abstract class BindsApplicationModule {
    @Singleton
    @Binds
    abstract fun bindActivityResources(resources: BeaconActivityResources): ActivityResources
}

@Module
@InstallIn(ActivityComponent::class)
abstract class BindsActivityModule {
    @Binds
    abstract fun bindsSettingsFragment(fragment: BeaconSettingsFragment): SettingsFragment
}

@Module
@InstallIn(ApplicationComponent::class)
class ProvidesApplicationModule {
    @Singleton
    @Provides
    fun provideBuildResources(@ApplicationContext context: Context): BuildResources {
        return BeaconBuildResources(context)
    }

    @Provides
    fun provideCotFactory(
            prefs: SharedPreferences,
            buildResources: BuildResources,
            deviceUidRepository: IDeviceUidRepository,
            gpsRepository: IGpsRepository,
            batteryRepository: IBatteryRepository
    ): CotFactory {
        return BeaconCotFactory(
                prefs,
                buildResources,
                deviceUidRepository,
                gpsRepository,
                batteryRepository
        )
    }
}
