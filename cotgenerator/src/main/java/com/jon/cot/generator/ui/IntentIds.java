package com.jon.cot.generator.ui;

import java.util.UUID;

class IntentIds {
    public static final String EXTRA_EDIT_PRESET_PROTOCOL = randomString();
    public static final String EXTRA_EDIT_PRESET_ALIAS = randomString();
    public static final String EXTRA_EDIT_PRESET_ADDRESS = randomString();
    public static final String EXTRA_EDIT_PRESET_PORT = randomString();
    public static final String EXTRA_EDIT_PRESET_CLIENT_BYTES = randomString();
    public static final String EXTRA_EDIT_PRESET_CLIENT_PASSWORD = randomString();
    public static final String EXTRA_EDIT_PRESET_TRUST_BYTES = randomString();
    public static final String EXTRA_EDIT_PRESET_TRUST_PASSWORD = randomString();

    private static String randomString() {
        return UUID.randomUUID().toString();
    }
}
