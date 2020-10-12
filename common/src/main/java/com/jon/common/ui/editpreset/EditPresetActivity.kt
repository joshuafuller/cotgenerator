package com.jon.common.ui.editpreset

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jon.common.R
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getStringFromPair
import com.jon.common.prefs.parseIntFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.PresetRepository
import com.jon.common.ui.ServiceBoundActivity
import com.jon.common.utils.InputValidator
import com.jon.common.utils.Notify
import com.jon.common.utils.Protocol

class EditPresetActivity : ServiceBoundActivity() {

    private val presetRepository = PresetRepository.getInstance()
    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.let {
            it.setTitle(R.string.toolbar_header_edit_preset)
            it.setDisplayHomeAsUpEnabled(true)
        }

        /* Pass all inter-activity arguments directly to the fragment */
        val fragment = EditPresetFragment()
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_preset_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val resId = item.itemId
        if (resId == android.R.id.home) {
            onBackPressed()
        } else if (resId == R.id.save) {
            if (settingsAreValid()) {
                if (EditPresetFragment.initialPresetValues == null) {
                    /* It's a new preset, so insert it into the database */
                    storePresetInDatabase()
                } else {
                    /* It's an update of an existing preset, so overwrite its DB record properly */
                    overwritePresetInDatabase()
                }
                passPresetBackToMainActivity()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        MaterialAlertDialogBuilder(this)
                .setTitle(R.string.go_back)
                .setMessage(R.string.go_back_message)
                .setPositiveButton(android.R.string.ok) { _, _ -> super.onBackPressed() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    private fun settingsAreValid(): Boolean {
        val inputValidator = InputValidator()
        try {
            /* Regular options */
            val protocolStr: String = prefs.getStringFromPair(CommonPrefs.PRESET_PROTOCOL)
            val alias: String = prefs.getStringFromPair(CommonPrefs.PRESET_ALIAS)
            val address: String = prefs.getStringFromPair(CommonPrefs.PRESET_DESTINATION_ADDRESS)
            val portStr: String = prefs.getStringFromPair(CommonPrefs.PRESET_DESTINATION_PORT)
            if (!inputValidator.validateString(protocolStr, ".*?")) {
                Notify.orange(getRootView(), "No protocol chosen!")
                return false
            } else if (!inputValidator.validateString(alias, ".*?")) {
                Notify.orange(getRootView(), "Enter an alias to describe the preset!")
                return false
            } else if (!inputValidator.validateString(address, ".*?")) {
                Notify.orange(getRootView(), "No destination address has been entered!")
                return false
            } else if (!inputValidator.validateInt(portStr, 1, 65535)) {
                Notify.orange(getRootView(), "Invalid port number!")
                return false
            }
            if (getInputProtocol() == Protocol.SSL) {
                /* SSL-only options */
                val clientBytes: String = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_CLIENTCERT_BYTES)
                val clientPass: String = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_CLIENTCERT_PASSWORD)
                val trustBytes: String = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_TRUSTSTORE_BYTES)
                val trustPass: String = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_TRUSTSTORE_PASSWORD)
                if (!inputValidator.validateString(clientBytes)) {
                    Notify.orange(getRootView(), "No client certificate chosen!")
                    return false
                } else if (!inputValidator.validateString(clientPass)) {
                    Notify.orange(getRootView(), "No client certificate password entered!")
                    return false
                } else if (!inputValidator.validateString(trustBytes)) {
                    Notify.orange(getRootView(), "No trust store chosen!")
                    return false
                } else if (!inputValidator.validateString(trustPass)) {
                    Notify.orange(getRootView(), "No trust store password entered!")
                    return false
                }
            }
        } catch (e: Exception) {
            Notify.orange(getRootView(), "Unknown error when validating preset: " + e.message)
            return false
        }
        return true
    }

    private fun getEnteredPresetValues(): OutputPreset {
        val protocol = getInputProtocol()
        val preset = OutputPreset(
                protocol,
                prefs.getStringFromPair(CommonPrefs.PRESET_ALIAS),
                prefs.getStringFromPair(CommonPrefs.PRESET_DESTINATION_ADDRESS),
                prefs.getStringFromPair(CommonPrefs.PRESET_DESTINATION_PORT).toInt()
        )
        if (protocol == Protocol.SSL) {
            preset.clientCert = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_CLIENTCERT_BYTES).toByteArray()
            preset.trustStore = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_TRUSTSTORE_BYTES).toByteArray()
            preset.clientCertPassword = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_CLIENTCERT_PASSWORD)
            preset.trustStorePassword = prefs.getStringFromPair(CommonPrefs.PRESET_SSL_TRUSTSTORE_PASSWORD)
        }
        return preset
    }

    private fun storePresetInDatabase() {
        presetRepository.insertPreset(getEnteredPresetValues())
    }

    private fun overwritePresetInDatabase() {
        val original = EditPresetFragment.initialPresetValues
        val updated = getEnteredPresetValues()
        if (original == null) {
            presetRepository.insertPreset(updated)
        } else {
            presetRepository.updatePreset(original, updated)
        }
    }

    private fun passPresetBackToMainActivity() {
        val preset = OutputPreset(
                getInputProtocol(),
                prefs.getStringFromPair(CommonPrefs.PRESET_ALIAS),
                prefs.getStringFromPair(CommonPrefs.PRESET_DESTINATION_ADDRESS),
                prefs.parseIntFromPair(CommonPrefs.PRESET_DESTINATION_PORT))
        val intent = Intent()
        intent.data = Uri.parse(preset.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun getInputProtocol() = Protocol.fromString(prefs.getStringFromPair(CommonPrefs.PRESET_PROTOCOL))
}