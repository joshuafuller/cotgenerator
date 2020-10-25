package com.jon.common.variants

import android.content.SharedPreferences
import android.os.Bundle
import androidx.navigation.NavDirections
import com.jon.common.cot.CursorOnTarget
import com.jon.common.service.CotFactory

object InjectorUtils {
    fun getBlankNavDirections(): NavDirections {
        return object : NavDirections {
            override fun getActionId() = 0
            override fun getArguments() = Bundle()
        }
    }

    fun getBlankCotFactory(prefs: SharedPreferences): CotFactory {
        return object : CotFactory(prefs) {
            override fun generate(): MutableList<CursorOnTarget> { return mutableListOf() }
            override fun initialise(): MutableList<CursorOnTarget> { return mutableListOf() }
            override fun update(): MutableList<CursorOnTarget> { return mutableListOf() }
            override fun clear() { }
        }
    }
}
