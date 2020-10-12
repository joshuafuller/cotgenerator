package com.jon.cotgenerator

import android.content.SharedPreferences
import com.jon.common.CotApplication
import com.jon.common.cot.CotRole
import com.jon.common.cot.CotTeam
import com.jon.common.cot.CursorOnTarget
import com.jon.common.cot.UtcTimestamp
import com.jon.common.prefs.*
import com.jon.common.service.CotFactory
import com.jon.common.service.Offset
import com.jon.common.service.Point
import com.jon.common.utils.Bearing
import com.jon.common.utils.Constants
import com.jon.common.utils.GeometryUtils.arcdistance
import com.jon.cotgenerator.streams.DoubleRandomStream
import com.jon.cotgenerator.streams.IntRandomStream
import com.jon.cotgenerator.streams.RadialDistanceRandomStream
import com.jon.cotgenerator.streams.RandomStream
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

internal class GeneratorCotFactory(prefs: SharedPreferences) : CotFactory(prefs) {
    private data class IconData(var cot: CursorOnTarget, var offset: Offset)

    private val random = Random(System.currentTimeMillis())

    private val useRandomCallsigns = prefs.getBooleanFromPair(GeneratorPrefs.RANDOM_CALLSIGNS)
    private val useRandomTeams = prefs.getBooleanFromPair(GeneratorPrefs.RANDOM_COLOUR)
    private val useRandomRoles = prefs.getBooleanFromPair(GeneratorPrefs.RANDOM_ROLE)
    private val iconCount = prefs.parseIntFromPair(GeneratorPrefs.ICON_COUNT)
    private val callsigns = getCallsigns()
    private val distributionRadius = prefs.parseDoubleFromPair(GeneratorPrefs.RADIAL_DISTRIBUTION)
    private val followGps = prefs.getBooleanFromPair(GeneratorPrefs.FOLLOW_GPS_LOCATION)
    private val centreLat = prefs.parseDoubleFromPair(GeneratorPrefs.CENTRE_LATITUDE)
    private val centreLon = prefs.parseDoubleFromPair(GeneratorPrefs.CENTRE_LONGITUDE)
    private val stayAtGroundLevel = prefs.getBooleanFromPair(GeneratorPrefs.STAY_AT_GROUND_LEVEL)
    private val centreAlt = if (stayAtGroundLevel) 0.0 else prefs.parseDoubleFromPair(GeneratorPrefs.CENTRE_ALTITUDE)
    private val staleTimer = prefs.getIntFromPair(CommonPrefs.STALE_TIMER).toLong()
    private var movementSpeed = prefs.parseDoubleFromPair(GeneratorPrefs.MOVEMENT_SPEED) * Constants.MPH_TO_METRES_PER_SECOND
    private var travelDistance: Double

    private lateinit var distributionCentre: Point
    private var icons = mutableListOf<IconData>()

    init {
        /* Stop any fuckery with distribution radii */
        movementSpeed = min(movementSpeed, distributionRadius / 2.0)
        travelDistance = movementSpeed * prefs.getIntFromPair(CommonPrefs.TRANSMISSION_PERIOD)
    }

    override fun clear() {
        icons.clear()
    }

    override fun generate(): List<CursorOnTarget> {
        return try {
            if (icons.isEmpty()) initialise() else update()
        } catch (e: ConcurrentModificationException) {
            ArrayList()
        }
    }

    override fun initialise(): List<CursorOnTarget> {
        icons = ArrayList()
        updateDistributionCentre()
        val now = UtcTimestamp.now()
        val distanceItr = weightedRadialIterator()
        val courseItr = doubleIterator(0.0, 360.0)
        val altitudeItr = doubleIterator(centreAlt - distributionRadius, centreAlt + distributionRadius)
        for (i in 0 until iconCount) {
            val cot = CursorOnTarget()
            cot.uid = "%s_%04d".format(deviceUidRepository.getUid(), i)
            cot.callsign = callsigns[i]
            cot.start = now
            cot.time = cot.start
            cot.setStaleDiff(staleTimer, TimeUnit.MINUTES)
            cot.team = CotTeam.fromPrefs(prefs, useRandomTeams)
            cot.role = CotRole.fromPrefs(prefs, useRandomRoles)
            cot.speed = movementSpeed
            cot.lat = distributionCentre.lat * Constants.RAD_TO_DEG
            cot.lon = distributionCentre.lon * Constants.RAD_TO_DEG
            cot.hae = initialiseAltitude(altitudeItr)
            cot.battery = batteryRepository.getPercentage()
            val initialOffset = generateInitialOffset(distanceItr, courseItr)
            setPositionFromOffset(cot, initialOffset)
            cot.course = initialOffset.theta
            icons.add(IconData(cot, initialOffset))
        }
        return getCotList()
    }

    override fun update(): List<CursorOnTarget> {
        updateDistributionCentre()
        val now = UtcTimestamp.now()
        val courseItr = doubleIterator(0.0, 360.0)
        icons.forEach {
            it.cot.start = now
            it.cot.time = now
            it.cot.setStaleDiff(staleTimer, TimeUnit.MINUTES)
            it.offset = generateBoundedOffset(courseItr, Point.fromCot(it.cot))
            val oldPoint = Point.fromCot(it.cot)
            setPositionFromOffset(it.cot, it.offset)
            val newPoint = Point.fromCot(it.cot)
            it.cot.course = Bearing.from(oldPoint).to(newPoint)
            it.cot.hae = updateAltitude(it.cot.hae)
            it.cot.battery = batteryRepository.getPercentage()
        }
        return getCotList()
    }

    private fun getCallsigns(): List<String> {
        val callsigns: MutableList<String> = ArrayList()
        if (useRandomCallsigns) {
            /* Grab the list of all valid callsigns and shuffle it into a random order */
            val resources = CotApplication.context.resources
            val allCallsigns = mutableListOf(*resources.getStringArray(R.array.atakCallsigns))
            allCallsigns.shuffle()
            /* Extract some at random */
            for (i in 0 until iconCount) {
                callsigns.add(allCallsigns[i % allCallsigns.size]) // modulus, just in case iconCount > allCallsigns.size
            }
        } else {
            /* Use custom callsign as entered in the settings */
            val baseCallsign = prefs.getStringFromPair(CommonPrefs.CALLSIGN)
            for (i in 0 until iconCount) {
                callsigns.add(String.format(Locale.ENGLISH, "%s-%d", baseCallsign, i))
            }
        }
        return callsigns
    }

    private fun setPositionFromOffset(cot: CursorOnTarget, newOffset: Offset) {
        val (lat, lon) = Point.fromCot(cot).add(newOffset)
        cot.lat = lat * Constants.RAD_TO_DEG
        cot.lon = lon * Constants.RAD_TO_DEG
    }

    private fun updateDistributionCentre() {
        distributionCentre = Point(
                lat = centreLatitudeDegrees() * Constants.DEG_TO_RAD,
                lon = centreLongitudeDegrees() * Constants.DEG_TO_RAD
        )
    }

    private fun generateInitialOffset(distanceItr: RandomStream<Double>, courseItr: RandomStream<Double>): Offset {
        return Offset(
                R = distanceItr.next(),
                theta = courseItr.next()
        )
    }

    private fun generateBoundedOffset(
            courseItr: RandomStream<Double>,
            startPoint: Point,
            attemptsRemaining: Int = MAX_OFFSET_GENERATION_ATTEMPTS
    ): Offset {
        if (attemptsRemaining == 0) {
            /* The recursive algorithm has failed too many times, so to avoid a stack overflow we just pick
             * a random point along the radius of the centrepoint and generate an Offset that will take us there */
            val offsetToEdge = Offset(travelDistance, courseItr.next())
            val nextPoint = distributionCentre.add(offsetToEdge)
            return Offset.from(startPoint).to(nextPoint)
        } else {
            val offset = Offset(travelDistance, courseItr.next())
            val endPoint = startPoint.add(offset)
            return if (arcdistance(endPoint, distributionCentre) > distributionRadius) {
                /* Invalid offset, so try again */
                generateBoundedOffset(courseItr, startPoint, attemptsRemaining - 1)
            } else {
                offset
            }
        }
    }

    private fun doubleIterator(min: Double, max: Double): RandomStream<Double> {
        return DoubleRandomStream(random, min, max)
    }

    private fun weightedRadialIterator(): RandomStream<Double> {
        return RadialDistanceRandomStream(random, distributionRadius)
    }

    private fun centreLatitudeDegrees() = if (followGps) {
        gpsRepository.latitude()
    } else {
        centreLat
    }

    private fun centreLongitudeDegrees() = if (followGps) {
        gpsRepository.longitude()
    } else {
        centreLon
    }

    private fun getCotList(): List<CursorOnTarget> {
        return icons.map { it.cot }
    }

    private fun initialiseAltitude(altitudeIterator: RandomStream<Double>): Double {
        return if (stayAtGroundLevel) {
            0.0
        } else {
            /* Can't have altitude below 0 */
            max(0.0, altitudeIterator.next())
        }
    }

    private fun updateAltitude(altitude: Double): Double {
        return if (stayAtGroundLevel) {
            0.0
        } else {
            /* Direction is either -1, 0 or +1; representing falling, staying steady or rising respectively */
            val direction = IntRandomStream(random, -1, 1).next()
            var newAltitude = altitude + direction * movementSpeed

            /* Not going below ground */
            if (newAltitude < 0.0) newAltitude = 0.0

            /* Clip within the bounds of the distribution radius*/
            if (newAltitude < centreAlt - distributionRadius) newAltitude = centreAlt - distributionRadius
            if (newAltitude > centreAlt + distributionRadius) newAltitude = centreAlt + distributionRadius
            newAltitude
        }
    }

    private companion object {
        const val MAX_OFFSET_GENERATION_ATTEMPTS = 10
    }
}
