package com.jon.cotgenerator.ui;

import java.util.UUID;

class IntentIds {
    public static final String EXTRA_EDIT_PRESET_PROTOCOL = randomString();
    public static final String EXTRA_EDIT_PRESET_ALIAS = randomString();
    public static final String EXTRA_EDIT_PRESET_ADDRESS = randomString();
    public static final String EXTRA_EDIT_PRESET_PORT = randomString();
    public static final String EXTRA_EDIT_PRESET_CLIENT_PATH = randomString();
    public static final String EXTRA_EDIT_PRESET_CLIENT_PASSWORD = randomString();
    public static final String EXTRA_EDIT_PRESET_TRUST_PATH = randomString();
    public static final String EXTRA_EDIT_PRESET_TRUST_PASSWORD = randomString();

    private static String randomString() {
        return UUID.randomUUID().toString();
    }
}
