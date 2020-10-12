package com.jon.common.presets

import androidx.room.*
import io.reactivex.Observable

@Dao
interface PresetDao {
    @Query("SELECT * FROM Presets WHERE Protocol LIKE :protocol")
    fun getByProtocol(protocol: String?): Observable<List<OutputPreset>>

    @Query("SELECT * FROM Presets WHERE Protocol LIKE :protocol AND Address LIKE :address AND Port LIKE :port LIMIT 1")
    fun getPreset(protocol: String?, address: String?, port: Int): OutputPreset?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(preset: OutputPreset)

    @Query("UPDATE Presets SET Protocol=:proto2, Alias=:alias2, Address=:addr2, Port=:port2, ClientCert=:client, ClientCertPassword=:clientPw, TrustStore=:trust, TrustStorePassword=:trustPw WHERE Protocol==:proto1 AND Address==:addr1 AND Port==:port1")
    fun update(proto1: String, addr1: String, port1: Int,
               proto2: String, addr2: String, port2: Int, alias2: String?,
               client: ByteArray?, clientPw: String?, trust: ByteArray?, trustPw: String?)

    @Delete
    fun delete(preset: OutputPreset)

    @Query("DELETE FROM Presets")
    fun deleteAll()
}
