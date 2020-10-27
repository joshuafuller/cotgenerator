package com.jon.common.service

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import androidx.preference.PreferenceManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.jon.common.CotApplication
import com.jon.common.repositories.GpsRepository
import com.jon.common.repositories.StatusRepository
import com.jon.common.utils.Notify
import com.jon.common.variants.Variant
import timber.log.Timber

class CotService : Service(),
        ThreadErrorListener {
    /* Repositories */
    private val gpsRepository = GpsRepository.getInstance()
    private val statusRepository = StatusRepository.getInstance()

    /* Service binding */
    inner class ServiceBinder : Binder() {
        val service = this@CotService
    }
    private val binder: IBinder = ServiceBinder()

    /* GPS fetching */
    private var updateRateSeconds = 0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private val locationCallback = GpsLocationCallback(gpsRepository)

    /* Settings */
    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    /* Thread management */
    private val threadManager by lazy { ThreadManager(prefs, this) }

    /* Notifications */
    private val notificationGenerator: NotificationGenerator by lazy { NotificationGenerator(this, prefs) }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.i("onDestroy")
        unregisterGpsUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("onStartCommand ${intent?.action}")
        when (intent?.action) {
            STOP_SERVICE -> shutdown()
            START_SERVICE -> start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun initialiseFusedLocationClient() {
        try {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this).apply {
                lastLocation.addOnSuccessListener { gpsRepository.setLocation(it) }
            }
            initialiseLocationRequest()
        } catch (e: SecurityException) {
            error(e)
        }
    }

    fun start() {
        Timber.i("Starting service")
        statusRepository.setStatus(ServiceState.RUNNING)
        threadManager.start()
        startForegroundService()
    }

    fun shutdown() {
        statusRepository.setStatus(ServiceState.STOPPED)
        Timber.i("Stopping service")
        threadManager.shutdown()
        stopForegroundService()
    }

    private fun error(throwable: Throwable) {
        Timber.e(throwable)
        if (!CotApplication.activityIsVisible) {
            /* No UI activities open, so post a toast which should(!) appear over any other apps in the foreground */
            val handler = Handler(Looper.getMainLooper())
            handler.post { Notify.toast(applicationContext, throwable.message!!) }
        }
        shutdown()
        ServiceState.errorMessage = throwable.message
        statusRepository.setStatus(ServiceState.ERROR)
    }

    fun updateGpsPeriod(newPeriodSeconds: Int) {
        updateRateSeconds = newPeriodSeconds
        initialiseLocationRequest()
    }

    private fun startForegroundService() {
        startForeground(3, notificationGenerator.getForegroundNotification())
    }

    private fun stopForegroundService() {
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
            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            error(e)
        }
    }

    private fun unregisterGpsUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    override fun onThreadError(throwable: Throwable) {
        /* Get an error from a thread, so pass the message down to our activity and show the user */
        error(throwable)
    }

    companion object {
        private val BASE_INTENT_ID = "${Variant.getAppId()}.CotService."
        val STOP_SERVICE = "${BASE_INTENT_ID}.STOP"
        val START_SERVICE = "${BASE_INTENT_ID}.START"
    }
}
