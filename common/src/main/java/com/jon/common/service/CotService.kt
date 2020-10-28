package com.jon.common.service

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.jon.common.CotApplication
import com.jon.common.repositories.IGpsRepository
import com.jon.common.repositories.IPresetRepository
import com.jon.common.repositories.IStatusRepository
import com.jon.common.utils.Notify
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class CotService : Service(),
        ThreadErrorListener {
    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var cotFactory: CotFactory

    @Inject
    lateinit var notificationGenerator: INotificationGenerator

    @Inject
    lateinit var gpsRepository: IGpsRepository

    @Inject
    lateinit var statusRepository: IStatusRepository

    @Inject
    lateinit var presetRepository: IPresetRepository

    inner class ServiceBinder : Binder() {
        val service = this@CotService
    }

    private val binder: IBinder = ServiceBinder()

    private var updateRateSeconds = 0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private val locationCallback by lazy { GpsLocationCallback(gpsRepository) }

    private val threadManager by lazy {
        ThreadManager(
                prefs = prefs,
                cotFactory = cotFactory,
                errorListener = this,
                presetRepository = presetRepository
        )
    }


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
        private val BASE_INTENT_ID = "com.jon.common.CotService."
        val STOP_SERVICE = "${BASE_INTENT_ID}.STOP"
        val START_SERVICE = "${BASE_INTENT_ID}.START"
    }
}
