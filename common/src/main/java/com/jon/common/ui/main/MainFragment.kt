package com.jon.common.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.jon.common.R
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.ui.ServiceCommunicator
import com.jon.common.utils.Notify
import com.jon.common.utils.Protocol
import com.jon.common.variants.Variant

/* Class to act as a wrapper to the SettingsFragment and the start/stop button view */
class MainFragment : Fragment() {

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private lateinit var startStopButton: Button
    private val serviceCommunicator by lazy { requireActivity() as ServiceCommunicator }

    private val startServiceOnClickListener = View.OnClickListener {
        if (presetIsSelected()) {
            serviceCommunicator.startService()
            showStopButton()
        } else {
            Notify.red(requireView(), "Select an output destination first!")
        }
    }

    private val stopServiceOnClickListener = View.OnClickListener {
        serviceCommunicator.stopService()
        showStartButton()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment, Variant.getSettingsFragment())
                .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.fragment_main, null)
        initialiseStartStopButton(view)
        return view
    }

    override fun onPause() {
        super.onPause()
        Notify.setAnchor(null)
    }

    private fun initialiseStartStopButton(view: View) {
        startStopButton = view.findViewById(Variant.getStartStopButtonId())
        Notify.setAnchor(startStopButton)
        val isRunning = if (serviceCommunicator.isServiceNull()) false else serviceCommunicator.isServiceRunning()
        if (isRunning) {
            showStopButton()
        } else {
            showStartButton()
        }
    }

    private fun showStopButton() {
        setButtonState(
                textId = R.string.button_stop,
                backgroundColourId = R.color.stop_button,
                foregroundColourId = R.color.white,
                iconId = R.drawable.stop,
                onClickListener = stopServiceOnClickListener
        )
    }

    private fun showStartButton() {
        setButtonState(
                textId = R.string.button_start,
                backgroundColourId = Variant.getAccentColourId(),
                foregroundColourId = R.color.black,
                iconId = R.drawable.start,
                onClickListener = startServiceOnClickListener
        )
    }

    private fun setButtonState(
            @StringRes textId: Int,
            @ColorRes backgroundColourId: Int,
            @ColorRes foregroundColourId: Int,
            @DrawableRes iconId: Int,
            onClickListener: View.OnClickListener
    ) {
        val context = requireContext()
        startStopButton.text = getString(textId)
        startStopButton.setBackgroundColor(ContextCompat.getColor(context, backgroundColourId))
        startStopButton.setTextColor(ContextCompat.getColor(context, foregroundColourId))
        startStopButton.setOnClickListener(onClickListener)

        val tintedIcon = DrawableCompat.wrap(ContextCompat.getDrawable(context, iconId)!!)
        DrawableCompat.setTint(tintedIcon, ContextCompat.getColor(context, foregroundColourId))
        startStopButton.setCompoundDrawablesWithIntrinsicBounds(tintedIcon, null, null, null)
    }

    private fun presetIsSelected(): Boolean {
        val presetPref = Protocol.fromPrefs(prefs).presetPref
        return !prefs.getString(CommonPrefs.DEST_ADDRESS, "").isNullOrEmpty() &&
                !prefs.getString(CommonPrefs.DEST_PORT, "").isNullOrEmpty() &&
                prefs.getStringFromPair(presetPref).split(OutputPreset.SEPARATOR).toTypedArray().isNotEmpty()
    }
}