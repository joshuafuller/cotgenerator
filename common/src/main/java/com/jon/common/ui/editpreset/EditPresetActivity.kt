package com.jon.common.ui.editpreset

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jon.common.R
import com.jon.common.presets.OutputPreset
import com.jon.common.repositories.PresetRepository
import com.jon.common.ui.ServiceBoundActivity
import com.jon.common.utils.*

class EditPresetActivity : ServiceBoundActivity() {

    private val presetRepository = PresetRepository.getInstance()

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setTitle(R.string.toolbarHeaderEditPreset)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        /* Pass all inter-activity arguments directly to the fragment */
        val fragment: Fragment = EditPresetFragment.newInstance()
        fragment.arguments = intent.extras
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, fragment)
                .commit()

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
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
                .setTitle(R.string.goBack)
                .setMessage(R.string.goBackMessage)
                .setPositiveButton(android.R.string.ok) { _, _ -> super.onBackPressed() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    private fun settingsAreValid(): Boolean {
        val inputValidator = InputValidator()
        try {
            /* Regular options */
            val protocolStr: String = PrefUtils.getString(prefs, Key.PRESET_PROTOCOL)
            val alias: String = PrefUtils.getString(prefs, Key.PRESET_ALIAS)
            val address: String = PrefUtils.getString(prefs, Key.PRESET_DESTINATION_ADDRESS)
            val portStr: String = PrefUtils.getString(prefs, Key.PRESET_DESTINATION_PORT)
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
                val clientBytes: String = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_BYTES)
                val clientPass: String = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_PASSWORD)
                val trustBytes: String = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_BYTES)
                val trustPass: String = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_PASSWORD)
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
                PrefUtils.getString(prefs, Key.PRESET_ALIAS),
                PrefUtils.getString(prefs, Key.PRESET_DESTINATION_ADDRESS),
                PrefUtils.getString(prefs, Key.PRESET_DESTINATION_PORT).toInt()
        )
        if (protocol == Protocol.SSL) {
            preset.clientCert = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_BYTES).toByteArray()
            preset.trustStore = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_BYTES).toByteArray()
            preset.clientCertPassword = PrefUtils.getString(prefs, Key.PRESET_SSL_CLIENTCERT_PASSWORD)
            preset.trustStorePassword = PrefUtils.getString(prefs, Key.PRESET_SSL_TRUSTSTORE_PASSWORD)
        }
        return preset
    }

    private fun storePresetInDatabase() {
        presetRepository.insertPreset(getEnteredPresetValues())
    }

    private fun overwritePresetInDatabase() {
        val original = EditPresetFragment.initialPresetValues
        val updated = getEnteredPresetValues()
        presetRepository.updatePreset(original, updated)
    }

    private fun passPresetBackToMainActivity() {
        val preset = OutputPreset(
                getInputProtocol(),
                PrefUtils.getString(prefs, Key.PRESET_ALIAS),
                PrefUtils.getString(prefs, Key.PRESET_DESTINATION_ADDRESS), PrefUtils.getString(prefs, Key.PRESET_DESTINATION_PORT).toInt())
        val intent = Intent()
        intent.data = Uri.parse(preset.toString())
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun getInputProtocol() = Protocol.fromString(PrefUtils.getString(prefs, Key.PRESET_PROTOCOL))
}