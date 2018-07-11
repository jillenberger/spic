package de.dbanalytics.spic.osm.graph;

import java.util.Map;

public interface RoutingService {

    Route query(double fromLon, double fromLat, double toLon, double toLat, Map<String, Object> parameters);

}
