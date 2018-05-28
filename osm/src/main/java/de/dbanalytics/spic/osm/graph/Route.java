package de.dbanalytics.spic.osm.graph;

import java.util.List;

public interface Route {

    double traveltime();

    double distance();

    List<RouteLeg> routeLegs();

}
