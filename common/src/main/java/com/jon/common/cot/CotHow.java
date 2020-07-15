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

    public static CotHow fromString(String howString) {
        for (CotHow how : values()) {
            if (how.get().equals(howString)) {
                return how;
            }
        }
        throw new IllegalArgumentException("Unknown CoT how: " + howString);
    }
}
