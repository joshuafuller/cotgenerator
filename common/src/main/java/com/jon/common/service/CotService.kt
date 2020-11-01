package com.jon.common.service

import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import androidx.lifecycle.LifecycleService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.jon.common.repositories.IGpsRepository
import com.jon.common.repositories.ISocketRepository
import com.jon.common.repositories.IStatusRepository
import com.jon.common.utils.VersionUtils
import timber.log.Timber
import javax.inject.Inject

abstract class CotService : LifecycleService(),
        IThreadErrorListener {
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
    lateinit var socketRepository: ISocketRepository

    @Inject
    lateinit var locationCallback: LocationCallback

    inner class ServiceBinder : Binder() {
        val service = this@CotService
    }

    private val binder: IBinder = ServiceBinder()

    private var updateRateSeconds = 0
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null

    private val threadManager by lazy {
        CotThreadManager(
                prefs = prefs,
                cotFactory = cotFactory,
                errorListener = this,
                socketRepository = socketRepository
        )
    }


    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        if (VersionUtils.isAtLeast(26)) {
            notificationGenerator.createForegroundChannel()
            notificationGenerator.createErrorChannel()
        }
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
                lastLocation.addOnSuccessListener {
                    if (it != null) {
                        gpsRepository.setLocation(it)
                    }
                }
            }
            initialiseLocationRequest()
        } catch (e: SecurityException) {
            error(e)
        }
    }

    open fun start() {
        Timber.i("Starting service")
        statusRepository.postStatus(ServiceState.RUNNING)
        threadManager.start()
        startForegroundService()
    }

    open fun shutdown() {
        statusRepository.postStatus(ServiceState.STOPPED)
        Timber.i("Stopping service")
        threadManager.shutdown()
        stopForegroundService()
    }

    private fun error(throwable: Throwable) {
        Timber.e(throwable)
        notificationGenerator.showErrorNotification(throwable.message)
        shutdown()
        ServiceState.errorMessage = throwable.message
        statusRepository.postStatus(ServiceState.ERROR)
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
        private const val BASE_INTENT_ID = "com.jon.common.CotService."
        const val STOP_SERVICE = "${BASE_INTENT_ID}.STOP"
        const val START_SERVICE = "${BASE_INTENT_ID}.START"
    }
}
