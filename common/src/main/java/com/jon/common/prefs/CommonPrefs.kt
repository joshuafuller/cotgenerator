package com.jon.common.prefs

import com.jon.common.R
import com.jon.common.utils.ResourceUtils

object CommonPrefs {
    /* CoT settings */
    val CALLSIGN = PrefPair.string(R.string.key_callsign, R.string.def_callsign)
    val TEAM_COLOUR = PrefPair.int(R.string.key_team_colour, R.integer.def_team_colour)
    val ICON_ROLE = PrefPair.string(R.string.key_icon_role, R.string.def_icon_role)
    val STALE_TIMER = PrefPair.int(R.string.key_stale_timer, R.integer.def_stale_timer)
    val TRANSMISSION_PERIOD = PrefPair.int(R.string.key_transmission_period, R.integer.def_transmission_period)
    val TRANSMISSION_PROTOCOL = PrefPair.string(R.string.key_transmission_protocol, R.string.def_transmission_protocol)

    /* Transmission settings */
    val DATA_FORMAT = PrefPair.string(R.string.key_data_format, R.string.def_data_format)
    val SSL_PRESETS = PrefPair.string(R.string.key_ssl_presets, R.string.def_ssl_preset)
    val TCP_PRESETS = PrefPair.string(R.string.key_tcp_presets, R.string.def_tcp_preset)
    val UDP_PRESETS = PrefPair.string(R.string.key_udp_presets, R.string.def_udp_preset)

    /* These are not preferences, just for displaying the current preset's details. Hence no defaults */
    val DEST_ADDRESS = ResourceUtils.getString(R.string.key_dest_address)
    val DEST_PORT = ResourceUtils.getString(R.string.key_dest_port)
    val EDIT_PRESETS = ResourceUtils.getString(R.string.key_edit_presets)

    /* Preset settings */
    val PRESET_PROTOCOL = PrefPair.string(R.string.key_preset_protocol, R.string.transmission_protocol_udp)
    val PRESET_ALIAS = PrefPair.string(R.string.key_preset_alias, R.string.unknown)
    val PRESET_DESTINATION_ADDRESS = PrefPair.string(R.string.key_preset_destination_address, R.string.unknown)
    val PRESET_DESTINATION_PORT = PrefPair.string(R.string.key_preset_destination_port, R.string.unknown)
    val PRESET_SSL_OPTIONS_CATEGORY = ResourceUtils.getString(R.string.key_ssl_options_category)
    val PRESET_SSL_CLIENTCERT_BYTES = PrefPair.string(R.string.key_preset_ssl_client_cert_bytes, R.string.unknown)
    val PRESET_SSL_CLIENTCERT_PASSWORD = PrefPair.string(R.string.key_preset_ssl_client_cert_password, R.string.unknown)
    val PRESET_SSL_TRUSTSTORE_BYTES = PrefPair.string(R.string.key_preset_ssl_trust_store_bytes, R.string.unknown)
    val PRESET_SSL_TRUSTSTORE_PASSWORD = PrefPair.string(R.string.key_preset_ssl_trust_store_password, R.string.unknown)

    /* Update Checking. This doesn't reference a resource string because it's not used in any XML files */
    val IGNORED_UPDATE_VERSIONS = "ignored_update_versions"
}
