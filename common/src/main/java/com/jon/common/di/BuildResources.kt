package com.jon.common.di

import com.jon.common.service.CotService

interface BuildResources {
    val appName: String
    val platform: String
    val buildTimestamp: String
    val appId: String
    val versionName: String
    val isDebug: Boolean
    val serviceClass: Class<out CotService>
}
