package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.PliCursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.enums.TeamColour;
import com.jon.cotgenerator.utils.Constants;
import com.jon.cotgenerator.utils.DeviceUid;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

class FakeCotGenerator extends CotGenerator {
    private Random random = new Random();
    private List<IconData> icons;
    private double movementSpeed;
    private double transmissionPeriod;
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
        distributionRadius = PrefUtils.parseDouble(prefs, Key.RADIAL_DISTRIBUTION);
        movementSpeed = PrefUtils.parseDouble(prefs, Key.MOVEMENT_SPEED) * Constants.MPH_TO_METRES_PER_SECOND;
        transmissionPeriod = PrefUtils.getInt(prefs, Key.TRANSMISSION_PERIOD);
        followGps = PrefUtils.getBoolean(prefs, Key.FOLLOW_GPS_LOCATION);
        centreLat = PrefUtils.parseDouble(prefs, Key.CENTRE_LATITUDE);
        centreLon = PrefUtils.parseDouble(prefs, Key.CENTRE_LONGITUDE);
        staleTimer = PrefUtils.getInt(prefs, Key.STALE_TIMER);

        /* Stop any fuckery with distribution radii */
        movementSpeed = Math.min(movementSpeed, distributionRadius/2.0);
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
        return (icons == null) ? initialise() : update();
    }

    @Override
    protected List<CursorOnTarget> initialise() {
        icons = new ArrayList<>();
        updateDistributionCentre();
        final int iconCount = PrefUtils.parseInt(prefs, Key.ICON_COUNT);
        final String callsign = PrefUtils.getString(prefs, Key.CALLSIGN);
        final UtcTimestamp now = UtcTimestamp.now();
        final double max_dLat = distributionRadius / Constants.EARTH_RADIUS;
        final double max_dLon = Math.abs(max_dLat / Math.cos(distributionCentre.lat));
        PrimitiveIterator.OfDouble latItr = randomIterator(max_dLat);
        PrimitiveIterator.OfDouble lonItr = randomIterator(max_dLon);
        PrimitiveIterator.OfDouble courseItr = randomIterator(0.0, 360.0);

        for (int i = 0; i < iconCount; i++) {
            PliCursorOnTarget cot = new PliCursorOnTarget();
            cot.uid = String.format(Locale.ENGLISH, "%s_%04d", DeviceUid.get(), i);
            cot.callsign = String.format(Locale.ENGLISH, "%s_%04d", callsign, i);
            cot.time = cot.start = now;
            cot.setStaleDiff(staleTimer, TimeUnit.MINUTES);
            cot.team = TeamColour.fromPrefs(prefs).team();
            cot.speed = movementSpeed;
            cot.course = courseItr.next();
            Point.Offset initialOffset = generateOffset(latItr, lonItr, distributionCentre, distributionRadius);
            setPositionFromOffset(cot, new Point.Offset(0.0,0.0), initialOffset);
            icons.add(new IconData(cot, initialOffset));
        }
        return getCot();
    }

    protected List<CursorOnTarget> update() {
        updateDistributionCentre();
        double travelDistance = movementSpeed * transmissionPeriod; // metres
        final double dlat = travelDistance / Constants.EARTH_RADIUS;
        final UtcTimestamp now = UtcTimestamp.now();
        PrimitiveIterator.OfDouble dlatItr = randomIterator(dlat);
        for (IconData icon : icons) {
            icon.cot.time = icon.cot.start = now;
            icon.cot.setStaleDiff(staleTimer, TimeUnit.MINUTES);
            PrimitiveIterator.OfDouble dlonItr = randomIterator(dlat / Math.cos(distributionCentre.lat));
            Point.Offset oldOffset = icon.offset;
            icon.offset = generateBoundedOffset(dlatItr, dlonItr, distributionCentre, distributionRadius, oldOffset, travelDistance);
            setPositionFromOffset(icon.cot, oldOffset, icon.offset);
            icon.cot.course = bearing(distributionCentre.add(oldOffset), distributionCentre.add(icon.offset));
        }
        return getCot();
    }

    private void setPositionFromOffset(CursorOnTarget cot, Point.Offset oldOffset, Point.Offset newOffset) {
        Point location =  distributionCentre.add(oldOffset).add(newOffset);
        cot.lat = location.lat * Constants.RAD_TO_DEG;
        cot.lon = location.lon * Constants.RAD_TO_DEG;
    }

    private void updateDistributionCentre() {
        distributionCentre = new Point(
                getCentreLatitudeDegrees() * Constants.DEG_TO_RAD,
                getCentreLongitudeDegrees() * Constants.DEG_TO_RAD
        );
    }

    private Point.Offset generateOffset(PrimitiveIterator.OfDouble dlat, PrimitiveIterator.OfDouble dlon, Point centre, double radius) {
        final Point.Offset offset = new Point.Offset(dlat.next(), dlon.next());
        final Point point = centre.add(offset);
        if (arcdistance(point, centre) > radius) {
            /* If the dot is outside the allowed radius, try generating again */
            return generateOffset(dlat, dlon, centre, radius);
        } else {
            /* If it's valid, return it */
            return offset;
        }
    }

    private Point.Offset generateBoundedOffset(PrimitiveIterator.OfDouble dlat, PrimitiveIterator.OfDouble dlon, Point distributionCentre,
                                               double distributionRadius, Point.Offset startOffset, double maxTravelDistance) {
        final Point startPoint = distributionCentre.add(startOffset);
        final Point.Offset offset = new Point.Offset(dlat.next(), dlon.next());
        final Point endPoint = startPoint.add(offset);

        if (/*arcdistance(endPoint, startPoint) > maxTravelDistance ||*/ arcdistance(endPoint, distributionCentre) > distributionRadius) {
            return generateBoundedOffset(dlat, dlon, distributionCentre, distributionRadius, startOffset, maxTravelDistance);
        } else {
            return offset.add(startOffset);
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
        final double lat1 = start.lat;
        final double lat2 = end.lat;
        final double dlon = end.lon-start.lon;

        final double y = Math.sin(dlon) * Math.cos(lat2);
        final double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dlon);
        return ( Math.atan2(y, x) * Constants.RAD_TO_DEG + 360.0 ) % 360.0;
    }

    private PrimitiveIterator.OfDouble randomIterator(double dx) {
        return randomIterator(-dx, dx);
    }

    private PrimitiveIterator.OfDouble randomIterator(double min, double max) {
        return random.doubles(min, max).iterator();
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
