package com.jon.common.ui.location

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager
import java.util.concurrent.TimeUnit

internal class Compass(private val context: Context) {
    data class Reading(val degrees: Double, val direction: String)

    private var lastUpdateTime: Long = System.nanoTime()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    fun shouldRecalculate(): Boolean {
        return System.nanoTime() - lastUpdateTime > SAMPLE_PERIOD_NS
    }

    fun getCompassReading(event: SensorEvent): Reading {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER ->
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            Sensor.TYPE_MAGNETIC_FIELD ->
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        val deviceOrientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
        var degrees = (Math.toDegrees(deviceOrientation[0].toDouble()) + 360.0) % 360.0

        /* Make any changes caused by device orientation. This is necessary because when in landscape, the 'degrees'
         * value calculated above assumes the device is "looking" towards its top, but in landscape we want it to "look"
         * to the side. */
        val screenOrientation = context.resources.configuration.orientation
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            /* Landscape mode, so query which way we're oriented: either 90 or 270 degrees */
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val screenRotation = windowManager.defaultDisplay.rotation
            degrees += if (screenRotation == Surface.ROTATION_90) 90 else -90
            degrees %= 360.0
        }

        val direction = getDirection(degrees)
        lastUpdateTime = System.nanoTime()
        return Reading(degrees, direction)
    }

    fun registerListener(sensorEventListener: SensorEventListener) {
        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SAMPLE_PERIOD_US
        )
        sensorManager.registerListener(
                sensorEventListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SAMPLE_PERIOD_US
        )
    }

    fun unregisterListener(sensorEventListener: SensorEventListener) {
        sensorManager.unregisterListener(sensorEventListener)
    }

    private fun getDirection(angle: Double): String {
        return when {
            angle < 11.25 -> "N"
            angle < 33.75 -> "NNE"
            angle < 56.25 -> "NE"
            angle < 78.75 -> "ENE"
            angle < 101.25 -> "E"
            angle < 123.75 -> "SEE"
            angle < 146.25 -> "SE"
            angle < 168.75 -> "SSE"
            angle < 191.25 -> "S"
            angle < 213.75 -> "SSW"
            angle < 236.25 -> "SW"
            angle < 258.75 -> "WSW"
            angle < 281.25 -> "W"
            angle < 303.75 -> "WNW"
            angle < 326.25 -> "NW"
            angle < 348.75 -> "NNW"
            else -> "N"
        }
    }

    companion object {
        private val SAMPLE_PERIOD_US = TimeUnit.MILLISECONDS.toMicros(100).toInt()
        private val SAMPLE_PERIOD_NS = SAMPLE_PERIOD_US * 1000
    }
}