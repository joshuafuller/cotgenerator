package com.jon.common.utils

import androidx.navigation.NavController
import androidx.navigation.NavDirections
import java.lang.IllegalArgumentException

fun NavController.safelyNavigate(directions: NavDirections) {
    try {
        navigate(directions)
    } catch (e: IllegalArgumentException) {
        /* Thrown if the user double taps on the menu item before the app has a chance to remove the button.
         * No-op, we just put this here to avoid app crashing */
    }
}
