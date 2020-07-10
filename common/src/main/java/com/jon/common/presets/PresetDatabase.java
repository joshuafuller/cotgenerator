package com.jon.common.presets;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {OutputPreset.class}, version = 3)
@TypeConverters({ ProtocolConverter.class })
public abstract class PresetDatabase extends RoomDatabase {
    static final String FILENAME = "presets.db";
    public abstract PresetDao presetDao();
}
