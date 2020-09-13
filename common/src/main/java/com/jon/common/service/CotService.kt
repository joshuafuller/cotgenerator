package com.jon.common.service

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.jon.common.BuildConfig
import com.jon.common.R
import com.jon.common.repositories.GpsRepository
import com.jon.common.ui.main.MainActivity
import com.jon.common.utils.GenerateInt
import com.jon.common.utils.Notify
import com.jon.common.utils.PrefUtils
import com.jon.common.variants.Variant
import timber.log.Timber
import java.util.*

abstract class CotService : Service(), ThreadErrorListener {
    inner class ServiceBinder : Binder() {
        val service = this@CotService
    }

    private val binder: IBinder = ServiceBinder()

    private var updateRateSeconds = 0
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationRequest: LocationRequest? = null
    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            gpsRepository.setLocation(locationResult.lastLocation)
        }
    }
    private val gpsRepository: GpsRepository = GpsRepository.getInstance()
    private lateinit var threadManager: ThreadManager
    var state = ServiceState.STOPPED
        private set
    private val stateListeners: MutableSet<ServiceStateListener> = HashSet()
    protected val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("onCreate")
        threadManager = ThreadManager(prefs, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
        unregisterGpsUpdates()
    }

    protected fun initialiseFusedLocationClient() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient.lastLocation.addOnSuccessListener { gpsRepository.setLocation(it) }
            initialiseLocationRequest()
        } catch (e: SecurityException) {
            error(e)
        }
    }

    fun start() {
        Timber.i("Starting service")
        state = ServiceState.RUNNING
        stateListeners.forEach { it.onServiceStateChanged(state) }
        threadManager.start()
        startForegroundService()
    }

    fun stop() {
        Timber.i("Stopping service")
        state = ServiceState.STOPPED
        stateListeners.forEach { it.onServiceStateChanged(state) }
        threadManager.shutdown()
        stopForegroundService()
    }

    private fun error(throwable: Throwable) {
        Timber.e(throwable)
        state = ServiceState.STOPPED
        if (stateListeners.isEmpty()) {
            /* No UI activities open, so post a toast which should(!) appear over any other apps in the foreground */
            val handler = Handler(Looper.getMainLooper())
            handler.post { Notify.toast(applicationContext, throwable.message!!) }
        } else {
            stateListeners.forEach { it.onServiceStateChanged(ServiceState.ERROR, throwable) }
        }
        threadManager.shutdown()
        stopForegroundService()
    }

    fun updateGpsPeriod(newPeriodSeconds: Int) {
        updateRateSeconds = newPeriodSeconds
        initialiseLocationRequest()
    }

    private fun startForegroundService() {
        /* Intent to launch main activity when tapping the notification */
        val launchPendingIntent = PendingIntent.getActivity(
                this,
                LAUNCH_ACTIVITY_PENDING_INTENT,
                Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        /* Intent to stop the service when the notification button is tapped */
        val stopIntent = Intent(this, CotService::class.java).setAction(STOP_SERVICE)
        val stopPendingIntent = PendingIntent.getService(this, STOP_SERVICE_PENDING_INTENT, stopIntent, 0)
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, Variant.getAppId())
                .setOngoing(true)
                .setSmallIcon(R.drawable.target)
                .setContentTitle(Variant.getAppName())
                .setContentText(PrefUtils.getPresetInfoString(prefs))
                .setContentIntent(launchPendingIntent)
                .addAction(R.drawable.stop, getString(R.string.stop), stopPendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.setCategory(Notification.CATEGORY_SERVICE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                    Variant.getAppId(),
                    Variant.getAppName(),
                    NotificationManager.IMPORTANCE_HIGH
            ).apply {
                lightColor = Color.BLUE
                lockscreenVisibility = Notification.VISIBILITY_SECRET
                enableVibration(false)
                setSound(null, null)
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        } else {
            notification.priority = NotificationCompat.PRIORITY_MAX
        }
        startForeground(3, notification.build())
    }

    private fun stopForegroundService() {
        state = ServiceState.STOPPED
        stopForeground(true)
        stopSelf()
    }

    private fun initialiseLocationRequest() {
        unregisterGpsUpdates()
        locationRequest = LocationRequest.create()
                .setInterval(updateRateSeconds * 1000.toLong())
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        registerGpsUpdates()
    }

    private fun registerGpsUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            error(e)
        }
    }

    private fun unregisterGpsUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun addStateListener(listener: ServiceStateListener) {
        stateListeners.add(listener)
        listener.onServiceStateChanged(state, null)
    }

    fun removeStateListener(listener: ServiceStateListener) {
        stateListeners.remove(listener)
    }

    override fun reportError(throwable: Throwable) {
        /* Get an error from a thread, so pass the message down to our activity and show the user */
        error(throwable)
    }

    companion object {
        private const val BASE_INTENT_ID = BuildConfig.LIBRARY_PACKAGE_NAME + ".CotService."
        const val STOP_SERVICE = BASE_INTENT_ID + "STOP"
        private val LAUNCH_ACTIVITY_PENDING_INTENT = GenerateInt.next()
        private val STOP_SERVICE_PENDING_INTENT = GenerateInt.next()
    }
}
