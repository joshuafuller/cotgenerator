package com.jon.cot.generator.presets;

import androidx.room.Dao;
import androidx.room.Delete;
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

    @Query("UPDATE Presets " +
            "SET Protocol=:proto2, Alias=:alias2, Address=:addr2, Port=:port2, ClientCert=:client, ClientCertPassword=:clientPw, TrustStore=:trust, TrustStorePassword=:trustPw " +
            "WHERE Protocol==:proto1 AND Address==:addr1 AND Port==:port1")
    void update(String proto1, String addr1, int port1,
                String proto2, String addr2, int port2, String alias2,
                byte[] client, String clientPw, byte[] trust, String trustPw);

    @Delete
    void delete(OutputPreset preset);

    @Query("DELETE FROM Presets")
    void deleteAll();
}