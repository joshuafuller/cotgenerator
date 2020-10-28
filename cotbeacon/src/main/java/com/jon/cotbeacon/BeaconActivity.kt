package com.jon.cotbeacon

import android.view.Menu
import android.view.MenuItem
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragmentDirections
import com.jon.common.utils.Notify

class BeaconActivity : MainActivity() {
    override val activityLayoutId = R.layout.beacon_activity
    override val menuResourceId = R.menu.beacon_main_menu
    override val navHostFragmentId = R.id.nav_host_fragment
    override val permissionRationaleId = R.string.permission_rationale

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
                    Notify.yellow(getRootView(), "CoT Beacon will now start transmitting on device boot!")
                } else {
                    Notify.blue(getRootView(), "CoT Beacon will no longer launch on device boot.")
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

