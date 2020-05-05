package com.jon.cotgenerator.service;

import com.jon.cotgenerator.utils.Constants;
import com.jon.cotgenerator.utils.GenerateInt;

import java.util.PrimitiveIterator;

class Point {
    private int id;
    double lat;
    double lon;

    Point(final double la, final double lo) {
        id = GenerateInt.next();
        lat = la;
        lon = lo;
    }

    Point() {
        id = GenerateInt.next();
    }

    static Point randomInCircle(final PrimitiveIterator.OfDouble lat, final PrimitiveIterator.OfDouble lon, final Point centre, final double radius) {
        final Point point = new Point(lat.next(), lon.next());
        if (arcdistance(centre, point) > radius) {
            /* The generated point is outside our radius, so take the next values of lat and lon from the sequences */
            return randomInCircle(lat, lon, centre, radius);
        } else {
            return point;
        }
    }

    private static double arcdistance(final Point p1, final Point p2) {
        final double phi1 = p1.lat;
        final double phi2 = p2.lat;
        final double dphi = phi2 - phi1;
        final double dtheta = (p2.lon - p1.lon);

        /* I can feel myself getting sweaty just looking at this */
        final double a = Math.sin(dphi/2) * Math.sin(dphi/2) + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dtheta/2) * Math.sin(dtheta/2);
        return 2 * Constants.EARTH_RADIUS * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}