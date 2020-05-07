package com.jon.cotgenerator.service;

import android.content.SharedPreferences;

import com.jon.cotgenerator.cot.CursorOnTarget;
import com.jon.cotgenerator.cot.UtcTimestamp;
import com.jon.cotgenerator.enums.TeamColour;
import com.jon.cotgenerator.utils.Constants;
import com.jon.cotgenerator.utils.Key;
import com.jon.cotgenerator.utils.PrefUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Random;

class FakeCotGenerator extends CotGenerator {
    private static final double DEG_TO_RAD = Math.PI / 180.0;
    private static final double RAD_TO_DEG = 1 / DEG_TO_RAD;

    private Map<Point, CursorOnTarget> iconsMap;
    private int movementRadius;

    FakeCotGenerator(SharedPreferences prefs) {
        super(prefs);
        movementRadius = PrefUtils.parseInt(prefs, Key.MOVEMENT_RADIUS);
    }

    @Override
    protected void clear() {
        if (iconsMap != null) {
            iconsMap.clear();
            iconsMap = null;
        }
    }

    @Override
    protected List<CursorOnTarget> generate() {
        return (iconsMap == null) ? initialise() : update();
    }

    @Override
    protected List<CursorOnTarget> initialise() {
        iconsMap = new HashMap<>();
        final int nCot = PrefUtils.parseInt(prefs, Key.ICON_COUNT);
        final String callsign = PrefUtils.getString(prefs, Key.CALLSIGN);
        final UtcTimestamp now = UtcTimestamp.now();
        final long staleTimer = 1000 * 60 * PrefUtils.getInt(prefs, Key.STALE_TIMER);
        final double distributionRadius = PrefUtils.parseDouble(prefs, Key.RADIAL_DISTRIBUTION);
        final Point centre = new Point(
                PrefUtils.parseDouble(prefs, Key.CENTRE_LATITUDE) * DEG_TO_RAD,
                PrefUtils.parseDouble(prefs, Key.CENTRE_LONGITUDE) * DEG_TO_RAD
        );
        final Random random = new Random();
        final double dlat = distributionRadius / Constants.EARTH_RADIUS;
        final double dlon = Math.abs(dlat / Math.cos(centre.lat));
        PrimitiveIterator.OfDouble latItr = randomIterator(random, centre.lat, dlat);
        PrimitiveIterator.OfDouble lonItr = randomIterator(random, centre.lon, dlon);

        for (int i = 0; i < nCot; i++) {
            CursorOnTarget cot = new CursorOnTarget();
            String uid = String.format(Locale.ENGLISH, "%s_%04d", callsign, i);
            cot.uid = uid;
            cot.callsign = uid;
            cot.time = now;
            cot.start = now;
            cot.setStaleDiff(staleTimer);
            cot.team = TeamColour.fromPrefs(prefs).team();
            final Point point = generatePoint(latItr, lonItr, centre, distributionRadius);
            cot.lat = point.lat * RAD_TO_DEG;
            cot.lon = point.lon * RAD_TO_DEG;
            iconsMap.put(point, cot);
        }
        return new ArrayList<>(iconsMap.values());
    }

    protected List<CursorOnTarget> update() {
        if (movementRadius > 0) {
            final Random rand = new Random();
            final double dlat = (double) movementRadius / Constants.EARTH_RADIUS;
            for (HashMap.Entry cotEntry : iconsMap.entrySet()) {
                Point centre = (Point) cotEntry.getKey();
                CursorOnTarget cot = (CursorOnTarget) cotEntry.getValue();
                final double dlon = dlat / Math.cos(centre.lat);

                PrimitiveIterator.OfDouble latItr = randomIterator(rand, centre.lat, dlat);
                PrimitiveIterator.OfDouble lonItr = randomIterator(rand, centre.lon, dlon);
                final Point p = generatePoint(latItr, lonItr, centre, movementRadius);
                cot.lat = p.lat * RAD_TO_DEG;
                cot.lon = p.lon * RAD_TO_DEG;
                iconsMap.put(centre, cot);
            }
        }
        return new ArrayList<>(iconsMap.values());
    }

    private Point generatePoint(final PrimitiveIterator.OfDouble lat, final PrimitiveIterator.OfDouble lon, final Point centre, final double radius) {
        final Point point = new Point(lat.next(), lon.next());
        if (arcdistance(centre, point) > radius) {
            /* If the dot is outside the allowed radius, try generating again */
            return generatePoint(lat, lon, centre, radius);
        } else {
            /* If it's valid, return the Point */
            return point;
        }
    }

    private double arcdistance(final Point p1, final Point p2) {
        final double phi1 = p1.lat;
        final double phi2 = p2.lat;
        final double dphi = phi2 - phi1;
        final double dtheta = (p2.lon - p1.lon);

        /* I can feel myself getting sweaty just looking at this */
        final double a = Math.sin(dphi / 2) * Math.sin(dphi / 2) + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dtheta / 2) * Math.sin(dtheta / 2);
        return 2 * Constants.EARTH_RADIUS * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private PrimitiveIterator.OfDouble randomIterator(Random rand, double x, double dx) {
        return rand.doubles(x - dx, x + dx).iterator();
    }
}
