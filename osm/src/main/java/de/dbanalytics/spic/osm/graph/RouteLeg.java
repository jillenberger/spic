package de.dbanalytics.spic.osm.graph;

public interface RouteLeg {

    double traveltime();

    double distance();

    long[] nodes();

}
