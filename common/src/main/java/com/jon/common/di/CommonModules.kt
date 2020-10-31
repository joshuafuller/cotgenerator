package com.jon.common.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.jon.common.presets.DatabaseMigrations
import com.jon.common.presets.PresetDao
import com.jon.common.presets.PresetDatabase
import com.jon.common.repositories.*
import com.jon.common.repositories.impl.*
import com.jon.common.service.INotificationGenerator
import com.jon.common.service.NotificationGenerator
import com.jon.common.service.SocketFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
abstract class BindsApplicationModule {
    @Singleton
    @Binds
    abstract fun bindGps(repository: GpsRepository): IGpsRepository

    @Singleton
    @Binds
    abstract fun bindStatus(repository: StatusRepository): IStatusRepository
}

@InstallIn(ServiceComponent::class)
@Module
class ProvidesServiceModule {
    @Provides
    fun bindsNotificationGenerator(
            @ApplicationContext context: Context,
            prefs: SharedPreferences,
            buildResources: BuildResources
    ): INotificationGenerator {
        return NotificationGenerator(context, prefs, buildResources)
    }
}

@InstallIn(ApplicationComponent::class)
@Module
class ProvidesApplicationModule {
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideBattery(@ApplicationContext context: Context): IBatteryRepository {
        return BatteryRepository(context)
    }

    @Singleton
    @Provides
    fun provideDeviceUid(@ApplicationContext context: Context): IDeviceUidRepository {
        return DeviceUidRepository(context)
    }

    @Singleton
    @Provides
    fun providePreset(@ApplicationContext context: Context, presetDao: PresetDao): IPresetRepository {
        return PresetRepository(context, presetDao)
    }

    @Singleton
    @Provides
    fun provideSocketFactory(prefs: SharedPreferences, presetRepository: IPresetRepository): SocketFactory {
        return SocketFactory(prefs, presetRepository)
    }

    @Singleton
    @Provides
    fun provideSocketRepository(socketFactory: SocketFactory): ISocketRepository {
        return SocketRepository(socketFactory)
    }

    @Provides
    fun providePresetDao(appDatabase: PresetDatabase): PresetDao {
        return appDatabase.presetDao()
    }

    @Singleton
    @Provides
    fun providePresetDatabase(@ApplicationContext context: Context): PresetDatabase {
        return Room.databaseBuilder(context, PresetDatabase::class.java, PresetDatabase.FILENAME)
                .addMigrations(*DatabaseMigrations.allMigrations)
                .fallbackToDestructiveMigration()
                .build()
    }
}
