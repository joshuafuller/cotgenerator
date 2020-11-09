package com.jon.cotbeacon.ui

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jon.common.prefs.getBooleanFromPair
import com.jon.cotbeacon.R
import com.jon.cotbeacon.cot.EmergencyType
import com.jon.cotbeacon.databinding.EmergencyDialogBinding
import com.jon.cotbeacon.prefs.BeaconPrefs

internal class EmergencyDialogBuilder(
        context: Context,
        prefs: SharedPreferences,
        callback: (EmergencyType) -> Unit
) : MaterialAlertDialogBuilder(context) {

    private val binding = EmergencyDialogBinding.inflate(LayoutInflater.from(context), null, false)

    init {
        binding.spinner.setItems(EmergencyType.values().map { it.description })

        val defaultSelection = if (prefs.getBooleanFromPair(BeaconPrefs.EMERGENCY_ACTIVE)) {
            EmergencyType.CANCEL
        } else {
            EmergencyType.ALERT_911
        }
        binding.spinner.selectedIndex = indexOf(defaultSelection)

        setTitle(R.string.emergency_dialog_title)
        setView(binding.root)
        setNegativeButton(R.string.emergency_back, null)
        setPositiveButton(R.string.emergency_send) { dialog, _ ->
            callback(getSelectedEmergencyType())
            dialog.dismiss()
        }
    }

    private fun getSelectedEmergencyType(): EmergencyType {
        return EmergencyType.values()[binding.spinner.selectedIndex]
    }

    private fun indexOf(emergencyType: EmergencyType): Int {
        return EmergencyType.values().indexOf(emergencyType)
    }
}
