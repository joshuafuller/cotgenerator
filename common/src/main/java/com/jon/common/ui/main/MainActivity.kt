package com.jon.common.ui.main

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import com.jon.common.versioncheck.GithubRelease
import com.jon.common.versioncheck.UpdateChecker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import timber.log.Timber
import java.io.IOException

open class MainActivity : ServiceBoundActivity(),
        PermissionCallbacks,
        OnSharedPreferenceChangeListener {

    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val updateChecker = UpdateChecker()
    private val compositeDisposable = CompositeDisposable()

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

        compositeDisposable.add(
                updateChecker.fetchReleases()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { onReleasesFetched(it) },
                                { onReleaseFetchingFailure(it) }
                        )
        )
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        compositeDisposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        val start = menu.findItem(R.id.start)
        val stop = menu.findItem(R.id.stop)
        if (service != null) {
            start.isVisible = viewModel.currentState == ServiceState.STOPPED
            stop.isVisible = viewModel.currentState == ServiceState.RUNNING
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

    private fun onReleasesFetched(releases: List<GithubRelease>) {
        val latest = updateChecker.getLatestRelease(releases)
        if (latest != null && updateChecker.isNewerVersion(latest) && updateChecker.releaseIsNotIgnored(latest, prefs)) {
            val msg = "Installed = ${Variant.getVersionName()}\nLatest = ${latest.name}\n\n" +
                    "Would you like to visit the Github page to download it?\n\n"
            MaterialAlertDialogBuilder(this)
                    .setTitle("Update Available")
                    .setMessage(msg)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(latest.htmlUrl))
                        startActivity(browserIntent)
                    }.setNegativeButton("IGNORE") { dialog, _ ->
                        updateChecker.ignoreRelease(latest, prefs)
                        dialog.dismiss()
                    }.setNeutralButton("LATER", null)
                    .show()
        }
    }

    private fun onReleaseFetchingFailure(throwable: Throwable) {
        if (throwable !is IOException) {
            /* Don't show an error snackbar if the exception was due to network problems.
             * If the user is off-grid I don't want to annoy them. */
            Timber.e(throwable)
            Notify.red(getRootView(), "Failed to check latest version on Github: ${throwable.message}")
        }
    }

    companion object {
        private val PERMISSIONS_CODE = GenerateInt.next()
        val PERMISSIONS = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}
