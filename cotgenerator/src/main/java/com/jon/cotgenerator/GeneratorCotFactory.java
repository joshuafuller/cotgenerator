package com.jon.cotgenerator;

import android.content.Context;
import android.content.SharedPreferences;

import com.jon.common.cot.CotRole;
import com.jon.common.cot.CotTeam;
import com.jon.common.cot.CursorOnTarget;
import com.jon.common.cot.UtcTimestamp;
import com.jon.common.service.CotFactory;
import com.jon.common.service.Point;
import com.jon.common.utils.Constants;
import com.jon.common.utils.DeviceUid;
import com.jon.common.utils.Key;
import com.jon.common.utils.PrefUtils;
import com.jon.common.CotApplication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.DoubleStream;

public class GeneratorCotFactory extends CotFactory {
    private Random random = new Random();

    private List<IconData> icons;
    private List<String> callsigns;
    private int iconCount;
    private double movementSpeed;
    private double travelDistance;
    private double distributionRadius;
    private boolean followGps;
    private double centreLat;
    private double centreLon;
    private boolean stayAtGroundLevel;
    private double centreAlt;
    private long staleTimer;
    private Point distributionCentre;

    private static class IconData {
        CursorOnTarget cot;
        Point.Offset offset;
        IconData(CursorOnTarget cot, Point.Offset offset) {
            this.cot = cot;
            this.offset = offset;
        }
    }

    GeneratorCotFactory(SharedPreferences prefs) {
        super(prefs);
        iconCount = PrefUtils.parseInt(prefs, Key.ICON_COUNT);
        callsigns = getCallsigns();
        distributionRadius = PrefUtils.parseDouble(prefs, Key.RADIAL_DISTRIBUTION);
        movementSpeed = PrefUtils.parseDouble(prefs, Key.MOVEMENT_SPEED) * Constants.MPH_TO_METRES_PER_SECOND;
        followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        centreLat = PrefUtils.parseDouble(prefs, Key.CENTRE_LATITUDE);
        centreLon = PrefUtils.parseDouble(prefs, Key.CENTRE_LONGITUDE);
        stayAtGroundLevel = PrefUtils.getBoolean(prefs, Key.STAY_AT_GROUND_LEVEL);
        centreAlt = stayAtGroundLevel ? 0.0 : (double)PrefUtils.getInt(prefs, Key.CENTRE_ALTITUDE);
        staleTimer = PrefUtils.getInt(prefs, Key.STALE_TIMER);

        /* Stop any fuckery with distribution radii */
        movementSpeed = Math.min(movementSpeed, distributionRadius/2.0);
        travelDistance = movementSpeed * PrefUtils.getInt(prefs, Key.TRANSMISSION_PERIOD);
    }

    @Override
    protected void clear() {
        if (icons != null) {
            icons.clear();
            icons = null;
        }
    }

    @Override
    protected List<CursorOnTarget> generate() {
        try {
            return (icons == null) ? initialise() : update();
        } catch (ConcurrentModificationException e) {
            return new ArrayList<>();
        }
    }

    @Override
    protected List<CursorOnTarget> initialise() {
        icons = new ArrayList<>();
        updateDistributionCentre();
        final UtcTimestamp now = UtcTimestamp.now();
        PrimitiveIterator.OfDouble distanceItr = weightedRadialIterator();
        PrimitiveIterator.OfDouble courseItr = randomIterator(0.0, 360.0);
        PrimitiveIterator.OfDouble altitudeItr = randomIterator(centreAlt-distributionRadius, centreAlt+distributionRadius);
        for (int i = 0; i < iconCount; i++) {
            CursorOnTarget cot = new CursorOnTarget();
            cot.uid = String.format(Locale.ENGLISH, "%s_%04d", DeviceUid.get(), i);
            cot.callsign = callsigns.get(i);
            cot.time = cot.start = now;
            cot.setStaleDiff(staleTimer, TimeUnit.MINUTES);
            cot.team = CotTeam.fromPrefs(prefs);
            cot.role = CotRole.fromPrefs(prefs);
            cot.speed = movementSpeed;
            cot.lat = distributionCentre.lat * Constants.RAD_TO_DEG;
            cot.lon = distributionCentre.lon * Constants.RAD_TO_DEG;
            cot.hae = initialiseAltitude(altitudeItr);
            cot.battery = battery.getPercentage();
            Point.Offset initialOffset = generateInitialOffset(distanceItr, courseItr);
            setPositionFromOffset(cot, initialOffset);
            cot.course = initialOffset.theta;
            icons.add(new IconData(cot, initialOffset));
        }
        return getCot();
    }

    protected List<CursorOnTarget> update() {
        updateDistributionCentre();
        final UtcTimestamp now = UtcTimestamp.now();
        PrimitiveIterator.OfDouble courseItr = randomIterator(0.0, 360.0);
        for (IconData icon : icons) {
            icon.cot.time = icon.cot.start = now;
            icon.cot.setStaleDiff(staleTimer, TimeUnit.MINUTES);
            icon.offset = generateBoundedOffset(courseItr, Point.fromCot(icon.cot));
            Point oldPoint = Point.fromCot(icon.cot);
            setPositionFromOffset(icon.cot, icon.offset);
            icon.cot.course = bearing(oldPoint, Point.fromCot(icon.cot));
            icon.cot.hae = updateAltitude(icon.cot.hae);
            icon.cot.battery = battery.getPercentage();
        }
        return getCot();
    }

    private List<String> getCallsigns() {
        List<String> callsigns = new ArrayList<>();
        if (PrefUtils.getBoolean(prefs, Key.RANDOM_CALLSIGNS)) {
            /* Grab the list of all valid callsigns and shuffle it into a random order */
            Context context = CotApplication.getContext();
            List<String> allCallsigns = Arrays.asList(context.getResources().getStringArray(R.array.atakCallsigns));
            Collections.shuffle(allCallsigns);
            /* Extract some at random */
            for (int i = 0; i < iconCount; i++) {
                callsigns.add(allCallsigns.get(i % allCallsigns.size())); // modulus, just in case iconCount > allCallsigns.size
            }
        } else {
            /* Use custom callsign as entered in the settings */
            final String baseCallsign = PrefUtils.getString(prefs, Key.CALLSIGN);
            for (int i = 0; i < iconCount; i++) {
                callsigns.add(String.format(Locale.ENGLISH, "%s-%d", baseCallsign, i));
            }
        }
        return callsigns;
    }

    private void setPositionFromOffset(CursorOnTarget cot, Point.Offset newOffset) {
        Point location = Point.fromCot(cot).add(newOffset);
        cot.lat = location.lat * Constants.RAD_TO_DEG;
        cot.lon = location.lon * Constants.RAD_TO_DEG;
    }

    private void updateDistributionCentre() {
        distributionCentre = new Point(
                getCentreLatitudeDegrees() * Constants.DEG_TO_RAD,
                getCentreLongitudeDegrees() * Constants.DEG_TO_RAD
        );
    }

    private Point.Offset generateInitialOffset(PrimitiveIterator.OfDouble distanceItr, PrimitiveIterator.OfDouble courseItr) {
        return new Point.Offset(distanceItr.next(), courseItr.next());
    }

    private Point.Offset generateBoundedOffset(final PrimitiveIterator.OfDouble courseItr, final Point startPoint) {
        final Point.Offset offset = new Point.Offset(travelDistance, courseItr.next());
        final Point endPoint = startPoint.add(offset);
        if (arcdistance(endPoint, distributionCentre) > distributionRadius) {
            /* Invalid offset, so try again */
            return generateBoundedOffset(courseItr, startPoint);
        } else {
            return offset;
        }
    }

    private double arcdistance(final Point p1, final Point p2) {
        final double lat1 = p1.lat;
        final double lat2 = p2.lat;
        final double dlat = p2.lat - p1.lat;
        final double dlon = p2.lon - p1.lon;
        /* I can feel myself getting sweaty just looking at this */
        final double a = Math.sin(dlat / 2.0) * Math.sin(dlat / 2.0) + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dlon / 2.0) * Math.sin(dlon / 2.0);
        return 2.0 * Constants.EARTH_RADIUS * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
    }

    private double bearing(Point start, Point end) {
        final double y = Math.sin(end.lon-start.lon) * Math.cos(end.lat);
        final double x = Math.cos(start.lat) * Math.sin(end.lat) - Math.sin(start.lat) * Math.cos(end.lat) * Math.cos(end.lon-start.lon);
        return ( Math.atan2(y, x) * Constants.RAD_TO_DEG + 360.0 ) % 360.0;
    }

    private PrimitiveIterator.OfDouble randomIterator(double dx) {
        return randomIterator(-dx, dx);
    }

    private PrimitiveIterator.OfDouble randomIterator(double min, double max) {
        return random.doubles(min, max).iterator();
    }

    private PrimitiveIterator.OfDouble weightedRadialIterator() {
        final double bound = Math.pow(distributionRadius, 2.0);
        /* p(x) proportional to sqrt(x), hopefully?  */
        return DoubleStream.generate(() -> Math.sqrt(random.nextDouble() * bound)).iterator();
    }

    private double getCentreLatitudeDegrees() {
        if (followGps) {
            return gpsCoords.latitude();
        } else {
            return centreLat;
        }
    }

    private double getCentreLongitudeDegrees() {
        if (followGps) {
            return gpsCoords.longitude();
        } else {
            return centreLon;
        }
    }

    private List<CursorOnTarget> getCot() {
        List<CursorOnTarget> crumbs = new ArrayList<>();
        for (IconData iconData : icons) {
            crumbs.add(iconData.cot);
        }
        return crumbs;
    }

    private double initialiseAltitude(PrimitiveIterator.OfDouble altitudeIterator) {
        if (stayAtGroundLevel) {
            return 0.0;
        } else {
            /* Can't have altitude below 0 */
            return Math.max(0.0, altitudeIterator.next());
        }
    }

    private double updateAltitude(double altitude) {
        if (stayAtGroundLevel) {
            return 0.0;
        } else {
            /* Direction is either -1, 0 or +1; representing falling, staying steady or rising */
            final int direction = random.ints(1, -1, 1).iterator().next();
            double newAltitude = altitude + (direction * movementSpeed);

            /* Not going below ground */
            if (newAltitude < 0.0) newAltitude = 0.0;

            /* Clip within the bounds of the distribution radius*/
            if (newAltitude < centreAlt-distributionRadius) newAltitude = centreAlt-distributionRadius;
            if (newAltitude > centreAlt+distributionRadius) newAltitude = centreAlt+distributionRadius;

            return newAltitude;
        }
    }
}
