package com.jon.common.ui.main

import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputType
import androidx.preference.*
import androidx.preference.EditTextPreference.OnBindEditTextListener
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.PresetRepository
import com.jon.common.utils.InputValidator
import com.jon.common.utils.Key
import com.jon.common.utils.Notify
import com.jon.common.utils.Protocol
import com.jon.common.variants.Variant
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*

abstract class MainFragment : PreferenceFragmentCompat(),
        OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    protected val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireActivity()) }
    private val compositeDisposable = CompositeDisposable()
    private val presetRepository = PresetRepository.getInstance()

    protected open fun getPhoneInputKeys() = mutableListOf(Key.ICON_COUNT)

    protected open fun getSuffixes() = mutableMapOf(
            Key.CENTRE_LATITUDE to "degrees",
            Key.CENTRE_LONGITUDE to "degrees"
    )

    protected open fun getPrefValidationRationales() = mutableMapOf(
            Key.CALLSIGN to "Contains invalid character(s)"
    )

    protected open fun getSeekbarKeys() = mutableListOf(
            Key.STALE_TIMER,
            Key.TRANSMISSION_PERIOD
    )

    override fun onCreatePreferences(savedState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(Variant.getSettingsXmlId(), rootKey)

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
        findPreference<Preference>(Key.EDIT_PRESETS)?.let {
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(activity, Variant.getListActivityClass()))
                true
            }
        }
    }

    override fun onActivityCreated(state: Bundle?) {
        super.onActivityCreated(state)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStart() {
        super.onStart()
        updatePreferences()
    }

    override fun onResume() {
        super.onResume()
        for ((key, value) in getSuffixes()) {
            setPreferenceSuffix(prefs, key, value)
        }
        updatePreferences()
    }

    protected open fun updatePreferences() {
        toggleProtocolSettingVisibility()
        toggleDataFormatSettingVisibility()
        toggleEmulateMultipleUsersSettingVisibility()
        updatePresetPreferences()
        insertPresetAddressAndPort()
    }

    override fun onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        compositeDisposable.dispose()
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        val suffixes = getSuffixes()
        if (suffixes.containsKey(key)) {
            setPreferenceSuffix(this.prefs, key, suffixes[key])
        }
        when (key) {
            Key.TRANSMISSION_PROTOCOL -> {
                toggleProtocolSettingVisibility()
                toggleDataFormatSettingVisibility()
                toggleEmulateMultipleUsersSettingVisibility()
                insertPresetAddressAndPort()
            }
            Key.SSL_PRESETS, Key.TCP_PRESETS, Key.UDP_PRESETS -> insertPresetAddressAndPort()
        }
    }

    protected fun setPrefVisibleIfCondition(key: String, condition: Boolean) {
        findPreference<Preference>(key)?.isVisible = condition
    }

    private fun toggleProtocolSettingVisibility() {
        val protocol = Protocol.fromPrefs(prefs)
        setPrefVisibleIfCondition(Key.SSL_PRESETS, protocol == Protocol.SSL)
        setPrefVisibleIfCondition(Key.TCP_PRESETS, protocol == Protocol.TCP)
        setPrefVisibleIfCondition(Key.UDP_PRESETS, protocol == Protocol.UDP)
    }

    private fun toggleDataFormatSettingVisibility() {
        /* Data format is only relevant for UDP, since TAK Server only takes XML data */
        val showDataFormatSetting = Protocol.fromPrefs(prefs) == Protocol.UDP
        setPrefVisibleIfCondition(Key.DATA_FORMAT, showDataFormatSetting)
    }

    private fun toggleEmulateMultipleUsersSettingVisibility() {
        /* Can only construct multiple sockets for TCP or UDP */
        val selected = Protocol.fromPrefs(prefs)
        val selectedTcpOrSsl = selected == Protocol.TCP || selected == Protocol.SSL
        setPrefVisibleIfCondition(Key.EMULATE_MULTIPLE_USERS, selectedTcpOrSsl)
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any): Boolean {
        val input = newValue as String
        val inputValidator = InputValidator()
        return when (val key = pref.key) {
            Key.CALLSIGN -> errorIfInvalid(input, key, inputValidator.validateCallsign(input))
            Key.TRANSMISSION_PERIOD -> errorIfInvalid(input, key, inputValidator.validateInt(input, 1, null))
            else -> true
        }
    }

    protected fun errorIfInvalid(input: String, key: String?, result: Boolean): Boolean {
        if (!result) {
            Notify.red(requireView(), "Invalid input: " + input + ". " + getPrefValidationRationales()[key])
        }
        return result
    }

    private fun setPreferenceSuffix(prefs: SharedPreferences?, key: String, suffix: String?) {
        val pref = findPreference<Preference>(key)
        if (pref != null) {
            val `val` = prefs!!.getString(key, "")
            pref.summary = String.format(Locale.ENGLISH, "%s %s", `val`, suffix)
        }
    }

    private fun insertPresetAddressAndPort() {
        val key = when (Protocol.fromPrefs(prefs)) {
            Protocol.SSL -> Key.SSL_PRESETS
            Protocol.TCP -> Key.TCP_PRESETS
            Protocol.UDP -> Key.UDP_PRESETS
        }
        val addressPref = findPreference<EditTextPreference>(Key.DEST_ADDRESS)
        val portPref = findPreference<EditTextPreference>(Key.DEST_PORT)
        val presetPref = findPreference<ListPreference>(key)
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
        compositeDisposable.add(presetRepository.getByProtocol(Protocol.SSL)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updatePresetEntries(it, Key.SSL_PRESETS) }) { notifyError(it) })
        compositeDisposable.add(presetRepository.getByProtocol(Protocol.TCP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updatePresetEntries(it, Key.TCP_PRESETS) }) { notifyError(it) })
        compositeDisposable.add(presetRepository.getByProtocol(Protocol.UDP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updatePresetEntries(it, Key.UDP_PRESETS) }) { notifyError(it) })
    }

    private fun updatePresetEntries(presets: List<OutputPreset>, prefKey: String) {
        val entries = OutputPreset.getAliases(presets).toTypedArray()
        val entryValues = presets.map { it.toString() }.toTypedArray()
        findPreference<ListPreference>(prefKey)?.let {
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

    private fun notifyError(throwable: Throwable) {
        Notify.red(requireView(), "Error: " + throwable.message)
        Timber.e(throwable)
    }

}