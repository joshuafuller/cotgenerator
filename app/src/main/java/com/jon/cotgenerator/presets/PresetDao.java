package com.jon.cotgenerator.presets;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import io.reactivex.Observable;

@Dao
public interface PresetDao {
    @Query("SELECT * FROM Presets WHERE Protocol LIKE :protocol")
    Observable<List<OutputPreset>> getByProtocol(String protocol);

    @Query("SELECT * FROM Presets WHERE Protocol LIKE :protocol AND Address LIKE :address AND Port LIKE :port LIMIT 1")
    OutputPreset getPreset(String protocol, String address, int port);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(OutputPreset preset);

    @Query("DELETE FROM Presets")
    void deleteAll();
}