package com.jon.common.ui.editpreset

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.text.InputType
import androidx.preference.*
import com.jon.common.R
import com.jon.common.presets.OutputPreset
import com.jon.common.ui.IntentIds
import com.jon.common.utils.*
import timber.log.Timber

class EditPresetFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val inputValidator = InputValidator()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.edit_preset, rootKey)

        /* Tell these two "bytes" preferences to launch a file browser when clicked */
        findPreference<Preference>(Key.PRESET_SSL_CLIENTCERT_BYTES)?.onPreferenceClickListener = fileBrowserOnClickListener(CLIENT_CERT_FILE_REQUEST_CODE)
        findPreference<Preference>(Key.PRESET_SSL_TRUSTSTORE_BYTES)?.onPreferenceClickListener = fileBrowserOnClickListener(TRUST_STORE_FILE_REQUEST_CODE)

        /* Restrict input types */
        PASSWORD_PREF_KEYS.forEach { findPreference<EditTextPreference>(it)?.setOnBindEditTextListener(PASSWORD_INPUT_TYPE) }
        findPreference<EditTextPreference>(Key.PRESET_DESTINATION_ADDRESS)?.setOnBindEditTextListener { it.inputType = InputType.TYPE_TEXT_VARIATION_URI }
        findPreference<EditTextPreference>(Key.PRESET_DESTINATION_PORT)?.setOnBindEditTextListener { it.inputType = InputType.TYPE_CLASS_PHONE }

        /* Set all prefs requiring validation to apply checks before committing any changes */
        PREFS_REQUIRING_VALIDATION.keys.forEach { findPreference<Preference>(it)?.onPreferenceChangeListener = this }
        prepopulateSpecifiedFields()
    }

    private fun prepopulateSpecifiedFields() {
        val bundle = arguments ?: return
        initialPresetValues = OutputPreset.blank().apply {
            /* Protocol */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_PROTOCOL)?.let {
                findPreference<ListPreference>(Key.PRESET_PROTOCOL)?.value = it
                this.protocol = Protocol.fromString(it)
            }
            /* Alias */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_ALIAS)?.let {
                findPreference<EditTextPreference>(Key.PRESET_ALIAS)?.text = it
                this.alias = it
            }
            /* Address */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_ADDRESS)?.let {
                findPreference<EditTextPreference>(Key.PRESET_DESTINATION_ADDRESS)?.text = it
                this.address = it
            }
            /* Port */
            bundle.getInt(IntentIds.EXTRA_EDIT_PRESET_PORT).let {
                findPreference<EditTextPreference>(Key.PRESET_DESTINATION_PORT)?.text = it.toString()
                this.port = it
            }
            /* Client cert bytes */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_CLIENT_BYTES)?.let {
                val length = if (inputValidator.validateString(it)) it.length else 0
                findPreference<Preference>(Key.PRESET_SSL_CLIENTCERT_BYTES)?.summary = "Loaded: $length bytes"
                prefs.edit().putString(Key.PRESET_SSL_CLIENTCERT_BYTES, it).apply()
                this.clientCert = it.toByteArray()
            }
            /* Client cert password */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_CLIENT_PASSWORD)?.let {
                findPreference<EditTextPreference>(Key.PRESET_SSL_CLIENTCERT_PASSWORD)?.text = it
                this.clientCertPassword = it
            }
            /* Trust store bytes */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_TRUST_BYTES)?.let {
                val length = if (inputValidator.validateString(it)) it.length else 0
                findPreference<Preference>(Key.PRESET_SSL_TRUSTSTORE_BYTES)?.summary = "Loaded: $length bytes"
                prefs.edit().putString(Key.PRESET_SSL_TRUSTSTORE_BYTES, it).apply()
                this.trustStore = it.toByteArray()
            }
            /* Trust store password */
            bundle.getString(IntentIds.EXTRA_EDIT_PRESET_TRUST_PASSWORD)?.let {
                findPreference<EditTextPreference>(Key.PRESET_SSL_TRUSTSTORE_PASSWORD)?.text = it
                this.trustStorePassword = it
            }
        }
    }

    private fun fileBrowserOnClickListener(requestCode: Int) = Preference.OnPreferenceClickListener {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-pkcs12" // .p12 files only
        }
        startActivityForResult(intent, requestCode)
        true
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
        toggleSSLSettingsVisibility()
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearPrefs()
    }

    @SuppressLint("DefaultLocale", "ApplySharedPref")
    override fun onActivityResult(requestCode: Int, resultCode: Int, result: Intent?) {
        if (result == null || result.data == null) return
        /* Get the preference key corresponding to the request code */
        val key = when (requestCode) {
            CLIENT_CERT_FILE_REQUEST_CODE -> Key.PRESET_SSL_CLIENTCERT_BYTES
            TRUST_STORE_FILE_REQUEST_CODE -> Key.PRESET_SSL_TRUSTSTORE_BYTES
            else -> throw RuntimeException("Unexpected request code: $requestCode")
        }
        if (resultCode != Activity.RESULT_OK) {
            return
        } else {
            try {
                val path = UriUtils.getPathFromUri(requireContext(), result.data!!)
                val bytes = FileUtils.toByteArray(path!!)
                prefs.edit().putString(key, String(bytes)).apply()
                findPreference<Preference>(key)?.summary = "Loaded: ${bytes.size} bytes"
            } catch (e: Exception) {
                Notify.red(requireView(), "Failed: ${e.message}")
                Timber.e(e)
                prefs.edit().remove(key).apply()
                findPreference<Preference>(key)?.summary = null
            }
            resetPassword(key)
        }
    }

    private fun resetPassword(key: String) {
        val passwordKey = when (key) {
            Key.PRESET_SSL_CLIENTCERT_BYTES -> Key.PRESET_SSL_CLIENTCERT_PASSWORD
            Key.PRESET_SSL_TRUSTSTORE_BYTES -> Key.PRESET_SSL_TRUSTSTORE_PASSWORD
            else -> throw IllegalArgumentException("Unexpected key $key")
        }
        findPreference<EditTextPreference>(passwordKey)?.let {
            it.text = null
            it.summary = null
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            Key.PRESET_SSL_CLIENTCERT_PASSWORD, Key.PRESET_SSL_TRUSTSTORE_PASSWORD -> setPasswordSummary(key)
            Key.PRESET_PROTOCOL -> toggleSSLSettingsVisibility()
        }
    }

    private fun setPasswordSummary(key: String) {
        findPreference<EditTextPreference>(key)?.let {
            val length = it.text.length
            it.summary = "*".repeat(length)
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val input = newValue as String
        return when (val key = preference.key) {
            Key.PRESET_DESTINATION_ADDRESS -> errorIfInvalid(input, key, inputValidator.validateHostname(input))
            Key.PRESET_DESTINATION_PORT -> errorIfInvalid(input, key, inputValidator.validateInt(input, 1, 65535))
            else -> true
        }
    }

    private fun errorIfInvalid(input: String, key: String, result: Boolean): Boolean {
        if (!result) {
            Notify.red(requireView(), "Invalid input: $input. ${PREFS_REQUIRING_VALIDATION[key]}")
        }
        return result
    }

    private fun setPrefVisibleIfCondition(key: String, condition: Boolean) {
        findPreference<Preference>(key)?.let {
            it.isVisible = condition
        }
    }

    private fun toggleSSLSettingsVisibility() {
        val sslIsSelected = try {
            Protocol.fromString(PrefUtils.getString(prefs, Key.PRESET_PROTOCOL)) == Protocol.SSL
        } catch (e: Exception) {
            /* thrown if the selected value is null */
            false
        }
        setPrefVisibleIfCondition(Key.PRESET_SSL_OPTIONS_CATEGORY, sslIsSelected)
    }

    @SuppressLint("ApplySharedPref")
    private fun clearPrefs() {
        val editor = prefs.edit()
        ALL_TEXT_PREFS.forEach { key ->
            editor.remove(key)
            findPreference<EditTextPreference>(key)?.let { it.text = null }
        }
        ALL_BYTES_PREFS.forEach { key ->
            editor.remove(key)
            findPreference<Preference>(key)?.let { it.summary = null }
        }
        editor.remove(Key.PRESET_PROTOCOL)
        findPreference<ListPreference>(Key.PRESET_PROTOCOL)?.value = null
        editor.commit()
    }

    companion object {
        private val CLIENT_CERT_FILE_REQUEST_CODE = GenerateInt.next()
        private val TRUST_STORE_FILE_REQUEST_CODE = GenerateInt.next()

        private val ALL_TEXT_PREFS = arrayOf(
                Key.PRESET_ALIAS,
                Key.PRESET_DESTINATION_ADDRESS,
                Key.PRESET_DESTINATION_PORT,
                Key.PRESET_SSL_CLIENTCERT_PASSWORD,
                Key.PRESET_SSL_TRUSTSTORE_PASSWORD
        )

        private val ALL_BYTES_PREFS = arrayOf(
                Key.PRESET_SSL_CLIENTCERT_BYTES,
                Key.PRESET_SSL_TRUSTSTORE_BYTES
        )

        private val PASSWORD_INPUT_TYPE = EditTextPreference.OnBindEditTextListener {
            it.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        private val PASSWORD_PREF_KEYS = arrayOf(
                Key.PRESET_SSL_CLIENTCERT_PASSWORD,
                Key.PRESET_SSL_TRUSTSTORE_PASSWORD
        )
        private val PREFS_REQUIRING_VALIDATION = hashMapOf(
                Key.PRESET_DESTINATION_ADDRESS to "Should be a valid network address",
                Key.PRESET_DESTINATION_PORT to "Should be an integer from 1-65355 inclusive"
        )

        var initialPresetValues: OutputPreset? = null

        fun newInstance(): EditPresetFragment {
            return EditPresetFragment()
        }
    }
}