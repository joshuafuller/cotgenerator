package com.jon.common.ui

import java.util.*

object IntentIds {
    val EXTRA_EDIT_PRESET_PROTOCOL = randomString()
    val EXTRA_EDIT_PRESET_ALIAS = randomString()
    val EXTRA_EDIT_PRESET_ADDRESS = randomString()
    val EXTRA_EDIT_PRESET_PORT = randomString()
    val EXTRA_EDIT_PRESET_CLIENT_BYTES = randomString()
    val EXTRA_EDIT_PRESET_CLIENT_PASSWORD = randomString()
    val EXTRA_EDIT_PRESET_TRUST_BYTES = randomString()
    val EXTRA_EDIT_PRESET_TRUST_PASSWORD = randomString()

    private fun randomString() = UUID.randomUUID().toString()
}
