package com.jon.common.ui.location

import android.location.Location
import org.opensextant.geodesy.*
import kotlin.math.abs

internal class GpsConverter {
    data class Converted(
            val latitude: String,
            val longitude: String,
            val mgrs: String,
            var altitudeMSL: String = UNKNOWN,
            var speedMetresPerSec: String = UNKNOWN,
            var bearing: String = UNKNOWN
    )

    fun convertCoordinates(location: Location?, format: CoordinateFormat): Converted {
        if (location == null) {
            return Converted(
                    latitude = NO_FIX,
                    longitude = NO_FIX,
                    mgrs = NO_FIX,
            )
        }
        val latitude = Latitude(location.latitude, Angle.DEGREES)
        val longitude = Longitude(location.longitude, Angle.DEGREES)
        return when (format) {
            CoordinateFormat.DD -> dd(latitude, longitude)
            CoordinateFormat.DM -> dm(latitude, longitude)
            CoordinateFormat.DMS -> dms(latitude, longitude)
            else -> dd(latitude, longitude)
        }.also {
            it.altitudeMSL = if (location.hasAltitude()) formatAltitude(location.altitude) else UNKNOWN
            it.speedMetresPerSec = if (location.hasSpeed()) formatSpeed(location.speed) else UNKNOWN
            it.bearing = if (location.hasBearing()) formatBearing(location.bearing) else UNKNOWN
        }
    }

    private fun dd(latitude: Latitude, longitude: Longitude): Converted {
        return Converted(
                latitude = "%3.6f".format(latitude.inDegrees()),
                longitude = "%3.6f".format(longitude.inDegrees()),
                mgrs = latLonToMgrs(latitude, longitude),
        )
    }

    private fun dm(latitude: Latitude, longitude: Longitude): Converted {
        return Converted(
                latitude = degreesToDm(latitude.inDegrees()),
                longitude = degreesToDm(longitude.inDegrees()),
                mgrs = latLonToMgrs(latitude, longitude),
        )
    }

    private fun dms(latitude: Latitude, longitude: Longitude): Converted {
        return Converted(
                latitude = degreesToDms(latitude.inDegrees()),
                longitude = degreesToDms(longitude.inDegrees()),
                mgrs = latLonToMgrs(latitude, longitude),
        )
    }

    private fun degreesToDm(decimalDegrees: Double): String {
        val degrees = decimalDegrees.toInt()
        val minutes = abs(decimalDegrees - degrees) * 60
        return "%3d° %2.4f'".format(degrees, minutes)
    }

    private fun degreesToDms(decimalDegrees: Double): String {
        val degrees = decimalDegrees.toInt()
        val minutes = (abs(decimalDegrees - degrees) * 60).toInt()
        val seconds = abs(decimalDegrees - degrees) * 60 - minutes
        return "%3d° %2d' %2.2f\"".format(degrees, minutes, seconds)
    }

    private fun latLonToMgrs(latitude: Latitude, longitude: Longitude): String {
        /* This comes in the format '30UAB1234567890', so we want to space it out into the various sections
         * to make it more readable */
        val squished = MGRS(longitude, latitude).toString()
        val zone = squished.substring(0, 3)
        val square = squished.substring(3, 5)
        val easting = squished.substring(5, 10)
        val northing = squished.substring(10)
        return "$zone $square $easting $northing"
    }

    private companion object {
        const val NO_FIX = "NO FIX"
        const val UNKNOWN = "???"

        private fun formatSpeed(speedMs: Float): String {
            return "%.1f m/s".format(speedMs)
        }

        private fun formatAltitude(altitudeMSL: Double): String {
            return "%.1fm WGS84".format(altitudeMSL)
        }

        private fun formatBearing(bearing: Float): String {
            return "%.1f° %s".format(bearing, AngleUtils.getDirection(bearing.toDouble()))
        }
    }
}
