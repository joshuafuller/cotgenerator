package com.jon.common.presets

import androidx.room.*
import com.jon.common.utils.Protocol

@Entity(tableName = "Presets", indices = [Index(value = ["id"], unique = true)])
class OutputPreset {
    /* Basic output fields */
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @ColumnInfo(name = "Protocol")
    var protocol: Protocol = Protocol.UDP

    @ColumnInfo(name = "Alias")
    var alias: String = ""

    @ColumnInfo(name = "Address")
    var address: String = ""

    @ColumnInfo(name = "Port")
    var port = 0

    /* SSL cert data. This are all null for UDP/TCP presets */
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "ClientCert")
    var clientCert: ByteArray? = null

    @ColumnInfo(name = "ClientCertPassword")
    var clientCertPassword: String? = null

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "TrustStore")
    var trustStore: ByteArray? = null

    @ColumnInfo(name = "TrustStorePassword")
    var trustStorePassword: String? = null

    @Ignore // Don't use this constructor for database initialisation
    constructor(protocol: Protocol, alias: String, address: String, port: Int)
            : this(protocol, alias, address, port, null, null, null, null)

    constructor(
            protocol: Protocol,
            alias: String,
            address: String,
            port: Int,
            clientCert: ByteArray? = null,
            clientCertPassword: String? = null,
            trustStore: ByteArray? = null,
            trustStorePassword: String? = null) {
        this.protocol = protocol
        this.alias = alias
        this.address = address
        this.port = port
        this.clientCert = clientCert
        this.clientCertPassword = clientCertPassword
        this.trustStore = trustStore
        this.trustStorePassword = trustStorePassword
    }

    private constructor() { /* blank */
    }

    override fun toString(): String {
        return protocol.toString() + SEPARATOR + alias + SEPARATOR + address + SEPARATOR + port
    }

    companion object {
        const val SEPARATOR = "¶" // pilcrow

        fun blank() = OutputPreset()

        fun getAliases(presets: List<OutputPreset>) = presets.map { it.alias }

        fun fromString(str: String?): OutputPreset? {
            if (str == null) return null
            val split = str.split(SEPARATOR).toTypedArray()
            if (split.size != 4) {
                throw RuntimeException("There should only be 4 elements in this string: " + str + ". Found " + split.size)
            }
            return OutputPreset(
                    Protocol.fromString(split[0]),
                    split[1],
                    split[2],
                    split[3].toInt()
            )
        }
    }
}