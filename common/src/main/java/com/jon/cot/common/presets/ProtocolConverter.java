package com.jon.cot.common.presets;

import androidx.room.TypeConverter;

import com.jon.cot.common.utils.Protocol;

class ProtocolConverter {
    @TypeConverter
    public static Protocol stringToProtocol(String string) {
        return Protocol.fromString(string);
    }

    @TypeConverter
    public static String protocolToString(Protocol protocol) {
        return protocol.get();
    }
}
