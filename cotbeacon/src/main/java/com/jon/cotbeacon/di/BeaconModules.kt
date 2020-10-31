package com.jon.cotbeacon.di

import android.content.Context
import android.content.SharedPreferences
import com.jon.common.di.IBuildResources
import com.jon.common.di.IUiResources
import com.jon.common.repositories.IBatteryRepository
import com.jon.cotbeacon.repositories.IChatRepository
import com.jon.common.repositories.IDeviceUidRepository
import com.jon.common.repositories.IGpsRepository
import com.jon.cotbeacon.repositories.ChatRepository
import com.jon.common.service.CotFactory
import com.jon.common.ui.main.SettingsFragment
import com.jon.cotbeacon.service.BeaconCotFactory
import com.jon.cotbeacon.ui.BeaconSettingsFragment
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
    abstract fun bindChat(repository: ChatRepository): IChatRepository
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
    fun provideBuildResources(@ApplicationContext context: Context): IBuildResources {
        return BeaconBuildResources(context)
    }

    @Singleton
    @Provides
    fun bindActivityResources(): IUiResources {
        return BeaconUiResources()
    }

    @Provides
    fun provideCotFactory(
            prefs: SharedPreferences,
            buildResources: IBuildResources,
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
