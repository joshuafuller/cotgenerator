package com.jon.common.ui.main

import android.Manifest
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.jon.common.R
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getIntFromPair
import com.jon.common.prefs.getStringFromPair
import com.jon.common.presets.OutputPreset
import com.jon.common.service.ServiceState
import com.jon.common.ui.AboutDialogBuilder
import com.jon.common.ui.ServiceBoundActivity
import com.jon.common.utils.GenerateInt
import com.jon.common.utils.Notify
import com.jon.common.utils.Protocol
import com.jon.common.variants.Variant
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import timber.log.Timber

open class MainActivity : ServiceBoundActivity(),
        PermissionCallbacks,
        OnSharedPreferenceChangeListener {

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
                .replace(R.id.fragment, Variant.getMainFragment())
                .commitNow()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
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
            start.isVisible = stateViewModel.currentState == ServiceState.STOPPED
            stop.isVisible = stateViewModel.currentState == ServiceState.RUNNING
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
                } else {
                    Notify.red(getRootView(), "Select an output destination first!")
                }
            }
            R.id.stop -> {
                service?.shutdown()
                invalidateOptionsMenu()
            }
            R.id.about -> {
                AboutDialogBuilder(this).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun presetIsSelected(): Boolean {
        val presetPref = Protocol.fromPrefs(prefs).presetPref
        return !prefs.getString(CommonPrefs.DEST_ADDRESS, "").isNullOrEmpty() &&
                !prefs.getString(CommonPrefs.DEST_PORT, "").isNullOrEmpty() &&
                prefs.getStringFromPair(presetPref).split(OutputPreset.SEPARATOR).toTypedArray().isNotEmpty()
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (CommonPrefs.TRANSMISSION_PERIOD.key == key) {
            service?.updateGpsPeriod(prefs.getIntFromPair(CommonPrefs.TRANSMISSION_PERIOD))
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
