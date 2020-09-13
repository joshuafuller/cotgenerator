package com.jon.common.utils

object Key {
    /* Main settings screen */
    const val CALLSIGN = "callsign"
    const val TEAM_COLOUR = "teamColour"
    const val ICON_ROLE = "iconRole"
    const val ICON_COUNT = "iconCount"
    const val STALE_TIMER = "staleTimer"
    const val TRANSMISSION_PERIOD = "transmissionPeriod"
    const val TRANSMISSION_PROTOCOL = "transmissionProtocol"
    const val DATA_FORMAT = "dataFormat"
    const val EMULATE_MULTIPLE_USERS = "emulateMultipleUsers"
    const val SSL_PRESETS = "sslPresets"
    const val TCP_PRESETS = "tcpPresets"
    const val UDP_PRESETS = "udpPresets"
    const val DEST_ADDRESS = "destAddress"
    const val DEST_PORT = "destPort"
    const val EDIT_PRESETS = "editPresets"

    /* Generator-only */
    const val RANDOM_CALLSIGNS = "randomCallsigns"
    const val RANDOM_COLOUR = "randomColour"
    const val RANDOM_ROLE = "randomRole"
    const val FOLLOW_GPS_LOCATION = "followMyGpsLocation"
    const val CENTRE_LATITUDE = "centreLatitude"
    const val CENTRE_LONGITUDE = "centreLongitude"
    const val STAY_AT_GROUND_LEVEL = "stayAtGroundLevel"
    const val CENTRE_ALTITUDE = "centreAltitude"
    const val RADIAL_DISTRIBUTION = "radialDistribution"
    const val MOVEMENT_SPEED = "movementSpeed"

    /* Edit preset screen */
    const val PRESET_PROTOCOL = "presetProtocol"
    const val PRESET_ALIAS = "presetAlias"
    const val PRESET_DESTINATION_ADDRESS = "presetDestinationAddress"
    const val PRESET_DESTINATION_PORT = "presetDestinationPort"
    const val PRESET_SSL_OPTIONS_CATEGORY = "sslOptionsCategory"
    const val PRESET_SSL_CLIENTCERT_BYTES = "presetSslClientCertBytes"
    const val PRESET_SSL_CLIENTCERT_PASSWORD = "presetSslClientCertPassword"
    const val PRESET_SSL_TRUSTSTORE_BYTES = "presetSslTrustStoreBytes"
    const val PRESET_SSL_TRUSTSTORE_PASSWORD = "presetSslTrustStorePassword"

    /* Beacon persistence */
    const val START_TRANSMITTING_ON_LAUNCH = "startTransmittingOnLaunch"
    const val START_TRANSMITTING_ON_BOOT = "startTransmittingOnBoot"
}