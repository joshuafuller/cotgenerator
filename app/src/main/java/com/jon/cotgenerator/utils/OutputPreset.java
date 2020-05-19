package com.jon.cotgenerator.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jon.cotgenerator.enums.Protocol;

import java.util.ArrayList;
import java.util.List;

public class OutputPreset {
    private static final String SEPARATOR = "¶"; // pilcrow

    public final Protocol protocol;
    public final String alias;
    public final String address;
    public final int port;

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

    public static List<OutputPreset> udpDefaults() {
        return new ArrayList<OutputPreset>() {{
            add(new OutputPreset(Protocol.UDP, "Default Multicast SA", "239.2.3.1", 6969));
        }};
    }

    public static List<OutputPreset> tcpDefaults() {
        return new ArrayList<OutputPreset>() {{
            add(new OutputPreset(Protocol.TCP, "Public TAK Server", "54.189.86.157", 8088));
            add(new OutputPreset(Protocol.TCP, "Public FreeTakServer", "204.48.30.216", 8087));
        }};
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
            Log.i("TAG", str);
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
