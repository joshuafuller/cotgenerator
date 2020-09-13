package com.jon.common.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.jon.common.variants.Variant
import com.jon.common.service.CotService
import com.jon.common.service.CotService.ServiceBinder
import com.jon.common.service.ServiceState
import com.jon.common.service.ServiceStateListener
import com.jon.common.utils.Notify

abstract class ServiceBoundActivity : AppCompatActivity(), ServiceStateListener {

    protected var service: CotService? = null

    private var serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as ServiceBinder).service
            service?.let {
                it.addStateListener(this@ServiceBoundActivity)
                onServiceStateChanged(it.state, null)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service!!.removeStateListener(this@ServiceBoundActivity)
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Start the service and bind to it */
        val intent = Intent(this, Variant.getCotServiceClass())
        startService(intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        service?.addStateListener(this)
    }

    override fun onPause() {
        super.onPause()
        service?.removeStateListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        service?.let {
            service = null
            unbindService(serviceConnection)
        }
    }

    protected fun getRootView(): View {
        return findViewById(android.R.id.content)
    }

    override fun onServiceStateChanged(state: ServiceState, throwable: Throwable?) {
        if (state == ServiceState.ERROR && throwable != null) {
            Notify.red(getRootView(), "Error: " + throwable.message)
        }
    }
}