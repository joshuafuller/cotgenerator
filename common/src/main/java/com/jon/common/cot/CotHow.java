package com.jon.common.cot;

public enum CotHow {
    HE("h-e"),
    MG("m-g"),
    HGIGO("h-g-i-g-o");

    private final String value;

    CotHow(String how) {
        this.value = how;
    }

    public String get() {
        return value;
    }

    public static CotHow from(String how) {
        switch (how) {
            case "h-e":       return HE;
            case "m-g":       return MG;
            case "h-g-i-g-o": return HGIGO;
            default:          throw new IllegalArgumentException("Unknown CoT how field: " + how);
        }
    }
}
