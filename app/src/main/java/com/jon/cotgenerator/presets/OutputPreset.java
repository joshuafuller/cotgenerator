package com.jon.cotgenerator.presets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.jon.cotgenerator.enums.Protocol;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

@Entity(tableName = "Presets", indices = { @Index(value = {"Address", "Port", "Protocol"}, unique = true) } )
public class OutputPreset {
    private static final String SEPARATOR = "¶"; // pilcrow

    @PrimaryKey int id;
    @ColumnInfo(name = "Protocol") public final Protocol protocol;
    @ColumnInfo(name = "Alias")    public final String alias;
    @ColumnInfo(name = "Address")  public final String address;
    @ColumnInfo(name = "Port")     public final int port;

    public OutputPreset(String protocolString, String alias, String address, int port) {
        this(Protocol.fromString(protocolString), alias, address, port);
    }

    OutputPreset(Protocol protocol, String alias, String address, int port) {
        this.protocol = protocol;
        this.alias = alias;
        this.address = address;
        this.port = port;
    }

    @NonNull
    @Override
    public String toString() {
        return protocol.get() + SEPARATOR + alias + SEPARATOR + address + SEPARATOR + port;
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
            Timber.e("There should only be 4 elements in this string: %s. Found %d", str, split.length);
            return null;
        }
        return new OutputPreset(
                Protocol.fromString(split[0]),
                split[1],
                split[2],
                Integer.parseInt(split[3])
        );
    }
}
