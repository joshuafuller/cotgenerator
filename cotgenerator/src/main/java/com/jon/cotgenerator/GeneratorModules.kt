package com.jon.cotgenerator

import android.content.Context
import android.content.SharedPreferences
import com.jon.common.di.IBuildResources
import com.jon.common.di.IUiResources
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
@InstallIn(ActivityComponent::class)
abstract class BindsActivityModule {
    @Binds
    abstract fun bindsSettingsFragment(fragment: GeneratorSettingsFragment): SettingsFragment
}

@Module
@InstallIn(ApplicationComponent::class)
class ProvidesApplicationModule {
    @Singleton
    @Provides
    fun provideBuildResources(@ApplicationContext context: Context): IBuildResources {
        return GeneratorBuildResources(context)
    }

    @Singleton
    @Provides
    fun bindActivityResources(): IUiResources {
        return GeneratorUiResources()
    }

    @Provides
    fun provideCotFactory(
            prefs: SharedPreferences,
            buildResources: IBuildResources,
            deviceUidRepository: IDeviceUidRepository,
            gpsRepository: IGpsRepository,
            batteryRepository: IBatteryRepository
    ): CotFactory {
        return GeneratorCotFactory(
                prefs,
                buildResources,
                deviceUidRepository,
                gpsRepository,
                batteryRepository
        )
    }
}
