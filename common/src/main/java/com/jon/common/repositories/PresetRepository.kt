package com.jon.common.repositories

import androidx.annotation.StringRes
import com.jon.common.CotApplication
import com.jon.common.R
import com.jon.common.presets.OutputPreset
import com.jon.common.presets.PresetDatabase
import com.jon.common.utils.FileUtils
import com.jon.common.utils.Protocol
import io.reactivex.Observable
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class PresetRepository private constructor() {
    private val database = PresetDatabase.build()
    private val executor: Executor = Executors.newSingleThreadExecutor()

    fun insertPreset(preset: OutputPreset) {
        executor.execute { database.presetDao().insert(preset) }
    }

    fun deletePreset(preset: OutputPreset) {
        executor.execute { database.presetDao().delete(preset) }
    }

    fun updatePreset(original: OutputPreset, updated: OutputPreset) {
        executor.execute {
            database.presetDao().update(
                    original.protocol.toString(),
                    original.address,
                    original.port,
                    updated.protocol.toString(),
                    updated.address,
                    updated.port,
                    updated.alias,
                    updated.clientCert,
                    updated.clientCertPassword,
                    updated.trustStore,
                    updated.trustStorePassword
            )
        }
    }

    fun getPreset(protocol: Protocol, address: String, port: Int): OutputPreset? {
        return database.presetDao().getPreset(protocol.toString(), address, port)
    }

    /* Returns a list of all presets; meaning custom presets and defaults. */
    fun getByProtocol(protocol: Protocol): Observable<List<OutputPreset>> {
        /* First get a list of known default presets, which aren't stored in the database */
        val defaults = Observable.just(defaultsByProtocol(protocol))
        /* Then query the database to grab any user-entered presets */
        return getCustomByProtocol(protocol)
                /* Merge the two observables together to return a single observable containing a list of OutputPresets */
                .zipWith(defaults) { fetchedList: List<OutputPreset>, defaultList: List<OutputPreset> ->
                    ArrayList<OutputPreset>().apply {
                        addAll(defaultList)
                        addAll(fetchedList)
                    }
                }
    }

    /* Returns a list of only those presets entered by the user */
    fun getCustomByProtocol(protocol: Protocol): Observable<List<OutputPreset>> {
        return database.presetDao().getByProtocol(protocol.toString())
    }

    fun deleteDatabase() {
        executor.execute { database.presetDao().deleteAll() }
    }

    fun defaultsByProtocol(protocol: Protocol): List<OutputPreset> {
        return when (protocol) {
            Protocol.SSL -> SSL_DEFAULTS
            Protocol.TCP -> TCP_DEFAULTS
            Protocol.UDP -> UDP_DEFAULTS
        }
    }

    companion object {
        private val instance = PresetRepository()
        fun getInstance() = instance

        private const val PASSWORD = "atakatak"
        private val UDP_DEFAULTS = listOf(
                OutputPreset(
                        Protocol.UDP,
                        getString(R.string.udpDefaultSa),
                        getString(R.string.udpDefaultSaIp),
                        getString(R.string.udpDefaultSaPort).toInt()
                )
        )
        private val TCP_DEFAULTS = listOf(
                OutputPreset(
                        Protocol.TCP,
                        getString(R.string.tcpFreetakserver),
                        getString(R.string.tcpFreetakserverIp),
                        getString(R.string.tcpFreetakserverPort).toInt()
                )
        )
        private val SSL_DEFAULTS = listOf(
                OutputPreset(
                        Protocol.SSL,
                        getString(R.string.sslTakserver),
                        getString(R.string.sslTakserverIp),
                        getString(R.string.sslTakserverPort).toInt(),
                        FileUtils.rawResourceToByteArray(R.raw.discord_client),
                        PASSWORD,
                        FileUtils.rawResourceToByteArray(R.raw.discord_truststore),
                        PASSWORD
                )
        )

        private fun getString(@StringRes id: Int): String {
            return CotApplication.context.getString(id)
        }
    }
}
