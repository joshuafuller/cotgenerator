package com.jon.cotbeacon.ui

import android.content.ComponentName
import android.content.SharedPreferences
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.jon.common.prefs.getBooleanFromPair
import com.jon.common.ui.main.MainActivity
import com.jon.common.ui.main.MainFragmentDirections
import com.jon.common.utils.Notify
import com.jon.common.utils.safelyNavigate
import com.jon.cotbeacon.R
import com.jon.cotbeacon.cot.ChatCursorOnTarget
import com.jon.cotbeacon.prefs.BeaconPrefs
import com.jon.cotbeacon.service.BeaconCotService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BeaconActivity : MainActivity(),
        IChatServiceCommunicator {

    private var chatMenuItem: MenuItem? = null
    private var emergencyMenuItem: MenuItem? = null

    private val beaconViewModel: BeaconActivityViewModel by viewModels()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val result = super.onCreateOptionsMenu(menu)

        /* Set the chat button's visibility on launch */
        chatMenuItem = menu.findItem(R.id.chat)
        chatMenuItem?.isVisible = chatIsEnabled()

        /* Set the emergency button's state on launch */
        emergencyMenuItem = menu.findItem(R.id.emergency)
        beaconViewModel.setEmergencyMenuItemState(emergencyMenuItem)
        return result
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.emergency ->
                dealWithEmergencyClick()
            R.id.location ->
                navController.safelyNavigate(uiResources.mainToLocationDirections)
            R.id.chat ->
                navController.safelyNavigate(MainFragmentDirections.actionMainToChat())
            R.id.about ->
                navController.safelyNavigate(uiResources.mainToAboutDirections)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        super.onServiceConnected(name, binder)
        val launchImmediately = prefs.getBooleanFromPair(BeaconPrefs.LAUNCH_FROM_OPEN)
        if (!viewModel.hasBeenCreatedAlready && launchImmediately && !isServiceRunning()) {
            /* Launch the service if a) the activity hasn't just undergone a config change,
             * b) we're configured to do so and b) it's not already running */
            service?.start()
        }
        viewModel.hasBeenCreatedAlready = true
    }

    override fun sendChat(chat: ChatCursorOnTarget) {
        getService()?.sendChat(chat)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            BeaconPrefs.ENABLE_CHAT.key ->
                chatMenuItem?.isVisible = chatIsEnabled()
        }
    }

    private fun chatIsEnabled(): Boolean {
        return prefs.getBooleanFromPair(BeaconPrefs.ENABLE_CHAT)
    }

    private fun dealWithEmergencyClick() {
        if (isServiceRunning()) {
            /* Ask the user which emergency type to send */
            EmergencyDialogBuilder(this, beaconViewModel.emergencyIsActive) {
                getService()?.sendEmergency(it)
                Notify.yellow(getRootView(), "Sent '${it.description}'")
                beaconViewModel.setEmergencyState(it)
                beaconViewModel.setEmergencyMenuItemState(emergencyMenuItem)
            }.show()
        } else {
            Notify.orange(getRootView(), "Start the service first!")
        }
    }

    private fun getService(): BeaconCotService? {
        return service as BeaconCotService?
    }
}

