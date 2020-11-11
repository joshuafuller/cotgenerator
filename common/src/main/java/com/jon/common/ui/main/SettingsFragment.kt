package com.jon.common.ui.main

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.preference.*
import androidx.preference.EditTextPreference.OnBindEditTextListener
import com.jon.common.di.IUiResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.PrefPair
import com.jon.common.prefs.RefreshCallsignPreference
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.IPresetRepository
import com.jon.common.utils.InputValidator
import com.jon.common.utils.Notify
import com.jon.common.utils.Protocol
import com.jon.common.utils.safelyNavigate
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.ArrayList

abstract class SettingsFragment : PreferenceFragmentCompat(),
        OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private val navController by lazy { findNavController() }

    @Inject
    lateinit var presetRepository: IPresetRepository

    @Inject
    protected lateinit var prefs: SharedPreferences

    @Inject
    protected lateinit var uiResources: IUiResources

    protected open fun getPhoneInputKeys() = mutableListOf<String>(
            /* blank, all phone inputs are in Generator only */
    )

    protected open fun getSuffixes() = mutableMapOf<String, String>(
            /* blank, all suffixes are in Generator only */
    )

    protected open fun getPrefValidationRationales() = mutableMapOf(
            CommonPrefs.CALLSIGN.key to "Contains invalid character(s)"
    )

    protected open fun getSeekbarKeys() = mutableListOf(
            CommonPrefs.STALE_TIMER.key,
            CommonPrefs.TRANSMISSION_PERIOD.key
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Timber.d("onCreateView")
        setHasOptionsMenu(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedState: Bundle?, rootKey: String?) {
        Timber.d("onCreatePreferences")
        setPreferencesFromResource(uiResources.settingsXmlId, rootKey)

        val callsignPreference = findPreference<RefreshCallsignPreference>(CommonPrefs.CALLSIGN.key)
        callsignPreference?.setUiResources(uiResources)

        /* Set numeric input on numeric fields */
        val phoneInputType = OnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_PHONE }
        getPhoneInputKeys().forEach {
            findPreference<EditTextPreference>(it)?.setOnBindEditTextListener(phoneInputType)
        }

        /* Register to intercept preference changes */
        getPrefValidationRationales().keys.forEach {
            findPreference<Preference>(it)?.onPreferenceChangeListener = this
        }

        /* Set the minimum seekbar values to 1, since the XML attribute for this doesn't work */
        getSeekbarKeys().forEach {
            findPreference<SeekBarPreference>(it)?.min = 1
        }

        /* Launch a new activity when clicking "Edit Presets" */
        findPreference<Preference>(CommonPrefs.EDIT_PRESETS)?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                Timber.d("Clicked custom presets")
                navController.safelyNavigate(uiResources.mainToListDirections)
                true
            }
        }
    }

    override fun onActivityCreated(state: Bundle?) {
        Timber.d("onActivityCreated")
        super.onActivityCreated(state)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        Timber.d("onStart")
        super.onStart()
        updatePreferences()
    }

    override fun onResume() {
        Timber.d("onResume")
        super.onResume()
        for ((key, value) in getSuffixes()) {
            setPreferenceSuffix(prefs, key, value)
        }
    }

    protected open fun updatePreferences() {
        Timber.d("updatePreferences")
        toggleProtocolSettingVisibility()
        toggleDataFormatSettingVisibility()
        updatePresetPreferences()
        insertPresetAddressAndPort()
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        Timber.d("onSharedPreferenceChanged %s", key)
        val suffixes = getSuffixes()
        if (suffixes.containsKey(key)) {
            setPreferenceSuffix(this.prefs, key, suffixes[key])
        }
        when (key) {
            CommonPrefs.TRANSMISSION_PROTOCOL.key -> {
                toggleProtocolSettingVisibility()
                toggleDataFormatSettingVisibility()
                insertPresetAddressAndPort()
            }
            CommonPrefs.SSL_PRESETS.key, CommonPrefs.TCP_PRESETS.key, CommonPrefs.UDP_PRESETS.key ->
                insertPresetAddressAndPort()
        }
    }

    protected fun <T> setPrefVisibleIfCondition(pref: PrefPair<T>, condition: Boolean) {
        Timber.d("setPrefVisibleIfCondition %s %s", pref.key, condition)
        findPreference<Preference>(pref.key)?.isVisible = condition
    }

    private fun toggleProtocolSettingVisibility() {
        val protocol = Protocol.fromPrefs(prefs)
        Timber.d("toggleProtocolSettingVisibility %s", protocol.name)
        setPrefVisibleIfCondition(CommonPrefs.SSL_PRESETS, protocol == Protocol.SSL)
        setPrefVisibleIfCondition(CommonPrefs.TCP_PRESETS, protocol == Protocol.TCP)
        setPrefVisibleIfCondition(CommonPrefs.UDP_PRESETS, protocol == Protocol.UDP)
    }

    private fun toggleDataFormatSettingVisibility() {
        /* Data format is only relevant for UDP, since TAK Server only takes XML data */
        val showDataFormatSetting = Protocol.fromPrefs(prefs) == Protocol.UDP
        Timber.d("toggleDataFormatSettingVisibility %s", showDataFormatSetting)
        setPrefVisibleIfCondition(CommonPrefs.DATA_FORMAT, showDataFormatSetting)
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any): Boolean {
        Timber.d("onPreferenceChange %s", newValue.toString())
        val input = newValue as String
        val inputValidator = InputValidator()
        return when (val key = pref.key) {
            CommonPrefs.CALLSIGN.key ->
                errorIfInvalid(input, key, inputValidator.validateCallsign(input))
            CommonPrefs.TRANSMISSION_PERIOD.key ->
                errorIfInvalid(input, key, inputValidator.validateInt(input, 1, null))
            else -> true
        }
    }

    protected fun errorIfInvalid(input: String, key: String?, result: Boolean): Boolean {
        Timber.d("errorIfInvalid %s %s %s", input, key, result)
        if (!result) {
            Notify.red(requireView(), "Invalid input: " + input + ". " + getPrefValidationRationales()[key])
        }
        return result
    }

    private fun setPreferenceSuffix(prefs: SharedPreferences, key: String, suffix: String?) {
        Timber.d("setPreferenceSuffix %s %s", key, suffix)
        findPreference<Preference>(key)?.let {
            val value = prefs.getString(key, "")
            it.summary = "$value $suffix"
        }
    }

    private fun insertPresetAddressAndPort() {
        Timber.d("insertPresetAddressAndPort")
        val addressPref = findPreference<EditTextPreference>(CommonPrefs.DEST_ADDRESS)
        val portPref = findPreference<EditTextPreference>(CommonPrefs.DEST_PORT)
        val presetPref = findPreference<ListPreference>(
                Protocol.fromPrefs(prefs).presetPref.key
        )
        if (addressPref != null && portPref != null && presetPref != null) {
            val preset = OutputPreset.fromString(presetPref.value)
            if (preset != null) {
                addressPref.text = preset.address
                portPref.text = preset.port.toString()
            } else {
                presetPref.value = null
                addressPref.text = null
                portPref.text = null
            }
        }
    }

    private fun updatePresetPreferences() {
        Timber.d("updatePresetPreferences")
        mapOf(
                Protocol.SSL to CommonPrefs.SSL_PRESETS.key,
                Protocol.TCP to CommonPrefs.TCP_PRESETS.key,
                Protocol.UDP to CommonPrefs.UDP_PRESETS.key,
        ).forEach { (protocol, key) ->
            presetRepository.getCustomByProtocol(protocol).observe(viewLifecycleOwner) { customPresets ->
                val allPresets = ArrayList<OutputPreset>().apply {
                    addAll(presetRepository.defaultsByProtocol(protocol))
                    addAll(customPresets)
                }
                updatePresetEntries(allPresets, key)
            }
        }
    }

    private fun updatePresetEntries(presets: List<OutputPreset>, key: String) {
        Timber.d("updatePresetEntries %s", key)
        val entries = presets.map { it.alias }.toTypedArray()
        val entryValues = presets.map { it.toString() }.toTypedArray()
        findPreference<ListPreference>(key)?.let {
            val previousValue = it.value
            it.entries = entries
            it.entryValues = entryValues
            if (entryValues.contains(previousValue)) {
                it.value = previousValue
            } else {
                it.setValueIndex(0)
            }
        }
    }

}