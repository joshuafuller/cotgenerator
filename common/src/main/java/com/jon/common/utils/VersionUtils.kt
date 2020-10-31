package com.jon.common.utils

import android.os.Build

object VersionUtils {
    fun isAtLeast(sdkMinimum: Int): Boolean {
        return Build.VERSION.SDK_INT >= sdkMinimum
    }

    fun isAtMost(sdkMaximum: Int): Boolean {
        return Build.VERSION.SDK_INT <= sdkMaximum
    }
}

object MinimumVersions {
    const val OKHTTP_MIN_SDK = 21
}