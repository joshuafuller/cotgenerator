package com.jon.common.cot;

public enum CotType {
    GROUND_COMBAT("a-f-G-U-C"),
    EMERGENCY_SEND("b-a-o-tbl"),
    EMERGENCY_CANCEL("b-a-o-can"),
    GEOCHAT("b-t-f");

    private final String value;

    CotType(String type) {
        this.value = type;
    }

    public String get() {
        return value;
    }

    public static CotType fromString(String typeString) {
        for (CotType type : values()) {
            if (type.get().equals(typeString)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown CoT type: " + typeString);
    }
}
