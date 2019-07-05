package de.dbanalytics.spic.gis;

public class HaversineCalculator {

    public static final double EARTH_RADIUS = 6371000;

    public static double distance(double fromLon, double fromLat, double toLon, double toLat) {
        double deltaRadLon = Math.toRadians(fromLon - toLon);
        double deltaRadLat = Math.toRadians(fromLat - toLat);

        double sinDeltaRadLon = Math.sin(deltaRadLon / 2.0);
        double sinDeltaRadLat = Math.sin(deltaRadLat / 2.0);

        double a = sinDeltaRadLat * sinDeltaRadLat +
                Math.cos(Math.toRadians(fromLat)) * Math.cos(Math.toRadians(toLat)) * sinDeltaRadLon * sinDeltaRadLon;

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
