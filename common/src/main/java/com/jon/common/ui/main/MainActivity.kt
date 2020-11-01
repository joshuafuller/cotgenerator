package com.jon.common.ui.main

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.Menu
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jon.common.R
import com.jon.common.di.IBuildResources
import com.jon.common.di.IUiResources
import com.jon.common.prefs.CommonPrefs
import com.jon.common.prefs.getIntFromPair
import com.jon.common.repositories.IStatusRepository
import com.jon.common.service.CotService
import com.jon.common.service.ServiceState
import com.jon.common.ui.IServiceCommunicator
import com.jon.common.ui.StateViewModel
import com.jon.common.utils.GenerateInt
import com.jon.common.utils.MinimumVersions
import com.jon.common.utils.Notify
import com.jon.common.utils.VersionUtils
import com.jon.common.versioncheck.GithubRelease
import com.jon.common.versioncheck.UpdateChecker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

abstract class MainActivity : AppCompatActivity(),
        EasyPermissions.PermissionCallbacks,
        OnSharedPreferenceChangeListener,
        ServiceConnection,
        IServiceCommunicator {

    protected var service: CotService? = null
    private val viewModel: StateViewModel by viewModels()
    private val compositeDisposable = CompositeDisposable()
    protected val navController: NavController by lazy { findNavController(uiResources.navHostFragmentId) }

    @Inject
    lateinit var updateChecker: UpdateChecker

    @Inject
    lateinit var statusRepository: IStatusRepository

    @Inject
    protected lateinit var prefs: SharedPreferences

    @Inject
    protected lateinit var uiResources: IUiResources

    @Inject
    protected lateinit var buildResources: IBuildResources

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (EasyPermissions.hasPermissions(this, *PERMISSIONS)) {
            buildActivity()
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    getString(uiResources.permissionRationaleId),
                    PERMISSIONS_CODE,
                    *PERMISSIONS
            )
        }
    }

    private fun buildActivity() {
        setContentView(uiResources.activityLayoutId)
        initialiseToolbar()
        if (VersionUtils.isAtLeast(MinimumVersions.OKHTTP_MIN_SDK)) {
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
        startCotService()
    }

    private fun initialiseToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
        toolbar.setNavigationOnClickListener {
            navController.navigateUp() || super.onSupportNavigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        service?.let {
            service = null
            unbindService(this)
        }
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        compositeDisposable.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(uiResources.mainMenuId, menu)
        return true
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

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as CotService.ServiceBinder).service
        service?.initialiseFusedLocationClient()
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
    }

    override fun startService() {
        service?.start()
    }

    override fun stopService() {
        service?.shutdown()
    }

    override fun isServiceNull(): Boolean {
        return service == null
    }

    override fun isServiceRunning(): Boolean {
        return viewModel.currentState == ServiceState.RUNNING
    }

    protected fun getRootView(): View {
        return findViewById(android.R.id.content)
    }

    private fun observeServiceStatus() {
        statusRepository.getStatus().observe(this) {
            viewModel.currentState = it
            if (it == ServiceState.ERROR) {
                Notify.red(getRootView(), "Error: ${ServiceState.errorMessage}")
            }
        }
    }

    private fun startCotService() {
        /* Start the service and bind to it */
        val intent = Intent(this, buildResources.serviceClass)
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)

        observeServiceStatus()
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
            val msg = "Installed = ${buildResources.versionName}\nLatest = ${latest.name}\n\n" +
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
