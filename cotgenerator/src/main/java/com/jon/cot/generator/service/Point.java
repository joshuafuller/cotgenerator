package com.jon.cot.generator.service;

import com.jon.cot.generator.cot.CursorOnTarget;
import com.jon.cot.generator.utils.Constants;

class Point {
    /* Both in radians */
    double lat;
    double lon;

    Point(final double lat, final double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    Point add(Offset offset) {
        final double rOverR = offset.R / Constants.EARTH_RADIUS;
        final double theta = Math.toRadians(offset.theta);  // travel bearing in radians
        final double lat1 = this.lat;                       // start latitude in radians
        final double lon1 = this.lon;                       // start longitude in radians
        final double lat2 = Math.asin( Math.sin(lat1)*Math.cos(rOverR) + Math.cos(lat1)*Math.sin(rOverR)*Math.cos(theta) );
        final double lon2 = lon1 + Math.atan2( Math.sin(theta)*Math.sin(rOverR)*Math.cos(lat1), Math.cos(rOverR)-Math.sin(lat1)*Math.sin(lat2) );
        return new Point(lat2, lon2);
    }

    static Point fromCot(CursorOnTarget cot) {
        return new Point(cot.lat * Constants.DEG_TO_RAD, cot.lon * Constants.DEG_TO_RAD);
    }

    static class Offset {
        double R;     /* travel distance in metres */
        double theta; /* bearing in degrees */

        Offset(double R, double theta) {
            this.R = R;
            this.theta = theta;
        }
    }
}