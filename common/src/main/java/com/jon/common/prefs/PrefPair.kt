package com.jon.common.prefs

import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import com.jon.common.utils.ResourceUtils

/* Small utility class to carry both a preference key and its default value in the same container. This is used alongside
 * the extension functions in SharedPreferenceExtensions.kt to abstract away (sort-of) the fetching of preference values
 * from the local data store. */
data class PrefPair<T>(val key: String, val default: T) {
    companion object {
        fun string(@StringRes keyId: Int, @StringRes valueId: Int) = PrefPair(
                ResourceUtils.getString(keyId), ResourceUtils.getString(valueId)
        )

        fun bool(@StringRes keyId: Int, @BoolRes valueId: Int) = PrefPair(
                ResourceUtils.getString(keyId), ResourceUtils.getBoolean(valueId)
        )

        fun int(@StringRes keyId: Int, @IntegerRes valueId: Int) = PrefPair(
                ResourceUtils.getString(keyId), ResourceUtils.getInt(valueId)
        )
    }
}
