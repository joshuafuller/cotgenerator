package com.jon.common.presets

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jon.common.CotApplication

@Database(entities = [OutputPreset::class], version = 4)
@TypeConverters(ProtocolConverter::class)
abstract class PresetDatabase : RoomDatabase() {
    abstract fun presetDao(): PresetDao

    companion object {
        private const val FILENAME = "presets.db"

        fun build(): PresetDatabase {
            return Room.databaseBuilder(CotApplication.context, PresetDatabase::class.java, FILENAME)
                    .addMigrations(*DatabaseMigrations.allMigrations)
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}
