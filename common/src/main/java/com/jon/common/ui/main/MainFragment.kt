package com.jon.common.ui.main

import android.content.SharedPreferences
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
import com.jon.common.R
import com.jon.common.di.IUiResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.IStatusRepository
import com.jon.common.service.ServiceState
import com.jon.common.ui.IServiceCommunicator
import com.jon.common.utils.Notify
import com.jon.common.utils.Protocol
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/* Class to act as a wrapper to the SettingsFragment and the start/stop button view */
@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var startStopButton: Button
    private val serviceCommunicator by lazy { requireActivity() as IServiceCommunicator }

    @Inject
    lateinit var uiResources: IUiResources

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var settingsFragment: SettingsFragment

    @Inject
    lateinit var statusRepository: IStatusRepository

    private val startServiceOnClickListener = View.OnClickListener {
        if (presetIsSelected()) {
            serviceCommunicator.startService()
        } else {
            Notify.red(requireView(), "Select an output destination first!")
        }
    }

    private val stopServiceOnClickListener = View.OnClickListener {
        serviceCommunicator.stopService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.beginTransaction()
                .replace(R.id.settings_fragment, settingsFragment)
                .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = View.inflate(context, R.layout.fragment_main, null)
        initialiseStartStopButton(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        statusRepository.getStatus().observe(viewLifecycleOwner) {
            if (it == ServiceState.RUNNING) {
                showStopButton()
            } else {
                showStartButton()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Notify.setAnchor(null)
    }

    private fun initialiseStartStopButton(view: View) {
        startStopButton = view.findViewById(uiResources.startStopButtonId)
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
                backgroundColourId = uiResources.accentColourId,
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