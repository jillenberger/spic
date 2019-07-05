package de.dbanalytics.spic.gis;

import junit.framework.TestCase;

public class HaversineCalculatorTest extends TestCase {

    private static final double MAX_ERROR = 0.003;

    public void testDistanceShort() {
        double latWelle = 50.117822;
        double lonWelle = 8.671924;

        double latPosthof = 50.107327;
        double lonPosthof = 8.659901;

        double ref = 1448;

        double d = HaversineCalculator.distance(lonWelle, latWelle, lonPosthof, latPosthof);

        assertEquals(ref, d, ref * MAX_ERROR);
    }

    public void testDistanceLong() {
        double latFra = 50.110973;
        double lonFra = 8.682126;

        double latBer = 52.520046;
        double lonBer = 13.404948;

        double ref = 424417;

        double d = HaversineCalculator.distance(lonFra, latFra, lonBer, latBer);

        assertEquals(ref, d, ref * MAX_ERROR);

    }
}
