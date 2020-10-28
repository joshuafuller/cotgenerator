package com.jon.cotbeacon

import android.view.Menu
import android.view.MenuItem
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragmentDirections
import com.jon.common.utils.Notify
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BeaconActivity : MainActivity() {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.launch_from_boot).isChecked = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_BOOT)
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.location ->
                navController.navigate(MainFragmentDirections.actionMainToLocation())
            R.id.about ->
                navController.navigate(MainFragmentDirections.actionMainToAbout())

            /* Deal with checkbox pressing, since Android doesn't invert the "checked status" of a
             * menu checkbox when selecting it. So here we invert that status, then put the new
             * value into shared preferences for persistence */
            R.id.launch_from_boot -> {
                val newValue = !item.isChecked
                item.isChecked = newValue
                prefs.edit()
                        .putBoolean(BeaconPrefs.LAUNCH_FROM_BOOT.key, newValue)
                        .apply()
                if (newValue) {
                    Notify.yellow(getRootView(), getString(R.string.start_from_boot))
                } else {
                    Notify.blue(getRootView(), getString(R.string.dont_start_from_boot))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

