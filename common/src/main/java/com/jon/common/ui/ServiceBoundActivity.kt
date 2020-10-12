package com.jon.common.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.jon.common.CotApplication
import com.jon.common.repositories.StatusRepository
import com.jon.common.service.CotService
import com.jon.common.service.CotService.ServiceBinder
import com.jon.common.service.ServiceState
import com.jon.common.utils.Notify
import com.jon.common.variants.Variant

abstract class ServiceBoundActivity : AppCompatActivity(),
        ServiceConnection {

    private val statusRepository = StatusRepository.getInstance()
    protected val viewModel: StateViewModel by viewModels()

    protected var service: CotService? = null

    protected fun startCotService() {
        /* Start the service and bind to it */
        val intent = Intent(this, CotService::class.java)
        startService(intent)
        bindService(intent, this, BIND_AUTO_CREATE)

        observeServiceStatus()
    }

    override fun onResume() {
        super.onResume()
        CotApplication.activityIsVisible = true
    }

    override fun onPause() {
        super.onPause()
        CotApplication.activityIsVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        service?.let {
            service = null
            unbindService(this)
        }
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder) {
        service = (binder as ServiceBinder).service
        service?.initialiseFusedLocationClient()
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
    }

    protected fun getRootView(): View {
        return findViewById(android.R.id.content)
    }

    private fun observeServiceStatus() {
        statusRepository.getStatus().observe(this) {
            viewModel.currentState = it
            invalidateOptionsMenu()
            when (it) {
                ServiceState.RUNNING -> Notify.green(getRootView(), "Service is running")
                ServiceState.STOPPED -> Notify.blue(getRootView(), "Service is not running")
                ServiceState.ERROR -> Notify.red(getRootView(), "Error: ${ServiceState.errorMessage}")
                else -> throw IllegalArgumentException("Unknown service state '$it'")
            }
        }
    }
}