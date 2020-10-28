package com.jon.common.prefs

import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import com.jon.common.CotApplication

internal object ResourceUtils {
    private val resources = CotApplication.context.resources

    fun getString(@StringRes stringId: Int, vararg args: Any?): String {
        return resources.getString(stringId, *args)
    }

    fun getBoolean(@BoolRes boolId: Int): Boolean {
        return resources.getBoolean(boolId)
    }

    fun getInt(@IntegerRes intId: Int): Int {
        return resources.getInteger(intId)
    }
}