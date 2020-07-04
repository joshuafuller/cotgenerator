package com.jon.cotgenerator.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.jon.cotgenerator.CotApplication;
import com.jon.cotgenerator.R;
import com.jon.cotgenerator.cot.CotRole;
import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.enums.TeamColour;
import com.jon.cotgenerator.utils.Constants;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.GenerateInt;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

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

class FakeCotGenerator extends CotGenerator {
    private Random random = new Random();
    private List<IconData> icons;
    private List<String> callsigns;
    private double movementSpeed;
    private double travelDistance;
    private double distributionRadius;
    private boolean followGps;
    private double centreLat;
    private double centreLon;
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

    FakeCotGenerator(SharedPreferences prefs) {
        super(prefs);
        callsigns = getShuffledCallsigns();
        distributionRadius = PrefUtils.parseDouble(prefs, Key.RADIAL_DISTRIBUTION);
        movementSpeed = PrefUtils.parseDouble(prefs, Key.MOVEMENT_SPEED) * Constants.MPH_TO_METRES_PER_SECOND;
        followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        centreLat = PrefUtils.parseDouble(prefs, Key.CENTRE_LATITUDE);
        centreLon = PrefUtils.parseDouble(prefs, Key.CENTRE_LONGITUDE);
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
        final int iconCount = PrefUtils.parseInt(prefs, Key.ICON_COUNT);
        final UtcTimestamp now = UtcTimestamp.now();
        PrimitiveIterator.OfDouble distanceItr = weightedRadialIterator();
        PrimitiveIterator.OfDouble courseItr = randomIterator(0.0, 360.0);
        for (int i = 0; i < iconCount; i++) {
            CursorOnTarget cot = new CursorOnTarget();
            cot.uid = String.format(Locale.ENGLISH, "%s_%04d", DeviceUid.get(), i);
            cot.callsign = randomCallsign(iconCount, i);
            cot.time = cot.start = now;
            cot.setStaleDiff(staleTimer, TimeUnit.MINUTES);
            cot.team = TeamColour.fromPrefs(prefs).get();
            cot.role = CotRole.fromPrefs(prefs);
            cot.speed = movementSpeed;
            cot.lat = distributionCentre.lat * Constants.RAD_TO_DEG;
            cot.lon = distributionCentre.lon * Constants.RAD_TO_DEG;
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
        }
        return getCot();
    }

    private List<String> getShuffledCallsigns() {
        Context context = CotApplication.getContext();
        List<String> callsigns = Arrays.asList(context.getResources().getStringArray(R.array.atakCallsigns));
        Collections.shuffle(callsigns);
        return callsigns;
    }

    private String randomCallsign(int iconCount, int iconIndex) {
        String callsign = callsigns.get(GenerateInt.random(callsigns.size()));
        if (iconCount > callsigns.size()) {
            callsign += "_" + iconIndex;
        }
        return callsign;
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
        final Random random = new Random();
        final double bound = Math.pow(distributionRadius, 2.0);
        /* p(x) proportional to sqrt(x), hopefully?  */
        return DoubleStream.generate(() -> Math.sqrt(random.nextDouble() * bound)).iterator();
    }

    private double getCentreLatitudeDegrees() {
        if (followGps) {
            return LastGpsLocation.latitude();
        } else {
            return centreLat;
        }
    }

    private double getCentreLongitudeDegrees() {
        if (followGps) {
            return LastGpsLocation.longitude();
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
}
