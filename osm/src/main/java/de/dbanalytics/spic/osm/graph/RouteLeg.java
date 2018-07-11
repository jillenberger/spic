package de.dbanalytics.spic.osm.graph;

public interface RouteLeg {

    double traveltime();

    double distance();

    String mode();

    String getAttribute(String key);

    long[] nodes();

}
