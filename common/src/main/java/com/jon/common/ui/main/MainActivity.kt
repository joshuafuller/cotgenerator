package com.jon.common.ui.main

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import com.jon.common.R
import com.jon.common.presets.OutputPreset
import com.jon.common.service.ServiceState
import com.jon.common.ui.AboutDialog
import com.jon.common.ui.ServiceBoundActivity
import com.jon.common.utils.GenerateInt
import com.jon.common.utils.Key
import com.jon.common.utils.Notify
import com.jon.common.utils.PrefUtils
import com.jon.common.variants.Variant
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import timber.log.Timber

open class MainActivity : ServiceBoundActivity(), PermissionCallbacks, OnSharedPreferenceChangeListener {
    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (EasyPermissions.hasPermissions(this, *PERMISSIONS)) {
            buildActivity()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    Variant.getPermissionRationale(),
                    PERMISSIONS_CODE,
                    *PERMISSIONS
            )
        }
    }

    private fun buildActivity() {
        setContentView(R.layout.activity)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            title = Variant.getAppName()
            setDisplayHomeAsUpEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, Variant.getSettingsFragment())
                .commitNow()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
        if (service != null) {
            /* Refresh our state on resuming */
            onServiceStateChanged(service!!.state, null)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val start = menu.findItem(R.id.start)
        val stop = menu.findItem(R.id.stop)
        if (service != null) {
            val state = service?.state ?: ServiceState.STOPPED
            start.isVisible = state == ServiceState.STOPPED
            stop.isVisible = state == ServiceState.RUNNING
        } else {
            start.isVisible = true
            stop.isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.start -> {
                if (presetIsSelected()) {
                    service?.start()
                    invalidateOptionsMenu()
                    Notify.green(getRootView(), "Service started")
                } else {
                    Notify.red(getRootView(), "Select an output destination first!")
                }
            }
            R.id.stop -> {
                service?.stop()
                invalidateOptionsMenu()
                Notify.blue(getRootView(), "Service stopped")
            }
            R.id.about -> {
                AboutDialog(this).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun presetIsSelected(): Boolean {
        val presetKey = PrefUtils.getPresetPrefKeyFromSharedPrefs(prefs)
        return PrefUtils.getString(prefs, Key.DEST_ADDRESS).isNotEmpty() &&
                PrefUtils.getString(prefs, Key.DEST_PORT).isNotEmpty() &&
                PrefUtils.getString(prefs, presetKey).split(OutputPreset.SEPARATOR).toTypedArray().size > 1
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, grantedPermissions: List<String>) {
        if (grantedPermissions.size == PERMISSIONS.size) {
            /* All permissions granted */
            buildActivity()
        } else {
            chastiseUserAndQuit()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        chastiseUserAndQuit()
    }

    override fun onServiceStateChanged(state: ServiceState, throwable: Throwable?) {
        invalidateOptionsMenu()
        super.onServiceStateChanged(state, throwable)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (Key.TRANSMISSION_PERIOD == key) {
            service?.updateGpsPeriod(PrefUtils.getInt(prefs, key))
        }
    }

    private fun chastiseUserAndQuit() {
        val err = "You need to grant all permissions!"
        Timber.e(err)
        Notify.toast(applicationContext, err)
        finish()
    }

    companion object {
        private val PERMISSIONS_CODE = GenerateInt.next()
        val PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}
