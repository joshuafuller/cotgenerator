package com.jon.cotbeacon

import android.content.ComponentName
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.StringRes
import com.jon.common.cot.ChatCursorOnTarget
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragmentDirections
import com.jon.common.utils.Notify
import com.jon.common.utils.safelyNavigate
import com.jon.cotbeacon.chat.IChatServiceCommunicator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BeaconActivity : MainActivity(),
        IChatServiceCommunicator {
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.launch_from_boot).isChecked = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_BOOT)
        menu.findItem(R.id.launch_from_open).isChecked = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_OPEN)
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.location ->
                navController.safelyNavigate(uiResources.mainToLocationDirections)
            R.id.chat ->
                navController.safelyNavigate(MainFragmentDirections.actionMainToChat())
            R.id.about ->
                navController.safelyNavigate(uiResources.mainToAboutDirections)
            R.id.launch_from_boot ->
                dealWithMenuCheckbox(
                        menuItem = item,
                        key = BeaconPrefs.LAUNCH_FROM_BOOT.key,
                        ifEnabled = R.string.start_from_boot,
                        ifDisabled = R.string.dont_start_from_boot
                )
            R.id.launch_from_open ->
                dealWithMenuCheckbox(
                        menuItem = item,
                        key = BeaconPrefs.LAUNCH_FROM_OPEN.key,
                        ifEnabled = R.string.start_from_open,
                        ifDisabled = R.string.dont_start_from_open
                )
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        super.onServiceConnected(name, binder)
        val launchImmediately = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_OPEN)
        if (launchImmediately && !isServiceRunning()) {
            /* Launch the service if a) we're configured to do so and b) it's not already running */
            service?.start()
        }
    }

    override fun sendChat(chat: ChatCursorOnTarget) {
        (service as BeaconCotService?)?.sendChat(chat)
    }

    /* Deal with checkbox pressing, since Android doesn't invert the "checked status" of a
     * menu checkbox when selecting it. So here we invert that status, then put the new
     * value into shared preferences for persistence */
    private fun dealWithMenuCheckbox(menuItem: MenuItem, key: String, @StringRes ifEnabled: Int, @StringRes ifDisabled: Int) {
        val newValue = !menuItem.isChecked
        menuItem.isChecked = newValue
        prefs.edit()
                .putBoolean(key, newValue)
                .apply()
        if (newValue) {
            Notify.yellow(getRootView(), getString(ifEnabled))
        } else {
            Notify.blue(getRootView(), getString(ifDisabled))
        }
    }
}

