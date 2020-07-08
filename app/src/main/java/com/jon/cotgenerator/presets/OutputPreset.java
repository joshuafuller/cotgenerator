package com.jon.cotgenerator.presets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.jon.cotgenerator.utils.Protocol;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Presets", indices = { @Index(value = {"Address", "Port", "Protocol"}, unique = true) } )
public class OutputPreset {
    public static final String SEPARATOR = "¶"; // pilcrow

    /* Basic output fields */
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "Protocol")
    public Protocol protocol;
    @ColumnInfo(name = "Alias")
    public String alias;
    @ColumnInfo(name = "Address")
    public String address;
    @ColumnInfo(name = "Port")
    public int port;

    /* SSL cert data */
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "ClientCert")
    public byte[] clientCert;
    @ColumnInfo(name = "ClientCertPassword")
    public String clientCertPassword;
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB, name = "TrustStore")
    public byte[] trustStore;
    @ColumnInfo(name = "TrustStorePassword")
    public String trustStorePassword;

    @Ignore // Don't use this constructor for database initialisation
    public OutputPreset(
            Protocol protocol,
            String alias,
            String address,
            int port) {
        this(protocol, alias, address, port, null, null, null, null);
    }

    public OutputPreset(
            Protocol protocol,
            String alias,
            String address,
            int port,
            byte[] clientCert,
            String clientCertPassword,
            byte[] trustStore,
            String trustStorePassword) {
        this.protocol = protocol;
        this.alias = alias;
        this.address = address;
        this.port = port;
        this.clientCert = clientCert;
        this.clientCertPassword = clientCertPassword;
        this.trustStore = trustStore;
        this.trustStorePassword = trustStorePassword;
    }

    private OutputPreset() { /* blank */ }

    @NonNull
    @Override
    public String toString() {
        return protocol.get() + SEPARATOR + alias + SEPARATOR + address + SEPARATOR + port;
    }

    public static OutputPreset blank() {
        return new OutputPreset();
    }

    public static List<String> getAliases(List<OutputPreset> presets) {
        List<String> aliases = new ArrayList<>();
        for (OutputPreset preset : presets) {
            aliases.add(preset.alias);
        }
        return aliases;
    }

    @Nullable
    public static OutputPreset fromString(String str) throws IllegalArgumentException {
        String[] split = str.split(SEPARATOR);
        if (split.length != 4) {
            throw new RuntimeException("There should only be 4 elements in this string: " + str + ". Found " + split.length);
        }
        return new OutputPreset(
                Protocol.fromString(split[0]),
                split[1],
                split[2],
                Integer.parseInt(split[3])
        );
    }
}
