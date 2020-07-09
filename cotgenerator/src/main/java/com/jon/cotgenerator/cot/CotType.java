package com.jon.cotgenerator.cot;

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

    public static CotType from(String type) {
        switch (type) {
            case "a-f-G-U-C": return GROUND_COMBAT;
            case "b-a-o-tbl": return EMERGENCY_SEND;
            case "b-a-o-can": return EMERGENCY_CANCEL;
            case "b-t-f":     return GEOCHAT;
            default:          throw new IllegalArgumentException("Unknown CoT type field: " + type);
        }
    }
}
