package de.dbanalytics.spic.osm.graph;

public interface RoutingService {

    Route query(double fromLon, double fromLat, double toLon, double toLat);

}
