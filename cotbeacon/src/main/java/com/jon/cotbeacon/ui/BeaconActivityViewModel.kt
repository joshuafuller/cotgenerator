package com.jon.cotbeacon.ui

import android.view.MenuItem
import androidx.lifecycle.ViewModel
import com.jon.cotbeacon.R
import com.jon.cotbeacon.cot.EmergencyType
import javax.inject.Inject

class BeaconActivityViewModel @Inject constructor() : ViewModel() {
    var emergencyIsActive = false
        private set

    fun setEmergencyState(emergencyType: EmergencyType) {
        emergencyIsActive = emergencyType != EmergencyType.CANCEL
    }

    fun setEmergencyMenuItemState(emergencyMenuItem: MenuItem?) {
        emergencyMenuItem?.setIcon(
                if (emergencyIsActive) {
                    R.drawable.emergency_active
                } else {
                    R.drawable.emergency_not_active
                }
        )
    }
}
