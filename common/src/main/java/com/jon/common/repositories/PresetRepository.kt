package com.jon.common.repositories

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jon.common.CotApplication
import com.jon.common.R
import com.jon.common.presets.OutputPreset
import com.jon.common.presets.PresetDatabase
import com.jon.common.utils.FileUtils
import com.jon.common.utils.Protocol
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

    fun getPreset(protocol: Protocol, address: String, port: Int): OutputPreset? {
        return database.presetDao().getPreset(protocol.toString(), address, port)
    }

    /* Returns a list of only those presets entered by the user */
    fun getCustomByProtocol(protocol: Protocol): LiveData<List<OutputPreset>> {
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
                        getString(R.string.udp_default_sa),
                        getString(R.string.udp_default_sa_ip),
                        getString(R.string.udp_default_sa_port).toInt()
                )
        )
        private val TCP_DEFAULTS = listOf(
                OutputPreset(
                        Protocol.TCP,
                        getString(R.string.tcp_freetakserver_name),
                        getString(R.string.tcp_freetakserver_ip),
                        getString(R.string.tcp_freetakserver_port).toInt()
                )
        )
        private val SSL_DEFAULTS = listOf(
                OutputPreset(
                        Protocol.SSL,
                        getString(R.string.ssl_takserver_name),
                        getString(R.string.ssl_takserver_ip),
                        getString(R.string.ssl_takserver_port).toInt(),
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
