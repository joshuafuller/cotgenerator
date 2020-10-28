package com.jon.common.di

interface BuildResources {
    val appName: String
    val platform: String
    val buildTimestamp: String
    val appId: String
    val versionName: String
    val isDebug: Boolean
}
