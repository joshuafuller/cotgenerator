package com.jon.common.ui.location

import android.location.GnssStatus
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class GnssCallback(private val listener: Listener) : GnssStatus.Callback() {

    override fun onStopped() {
        listener.onUsefulSatellitesReported(GNSS_STOPPED)
    }

    override fun onSatelliteStatusChanged(status: GnssStatus?) {
        if (status != null) {
            val usefulCount = (0 until status.satelliteCount)
                    .filter { status.usedInFix(it) }
                    .count()
            listener.onUsefulSatellitesReported(usefulCount)
        } else {
            listener.onUsefulSatellitesReported(0)
        }
    }

    companion object {
        const val GNSS_STOPPED = -1
    }

    interface Listener {
        fun onUsefulSatellitesReported(numUsefulSatellites: Int)
    }
}