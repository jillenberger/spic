package de.dbanalytics.spic.job;

import de.dbanalytics.spic.sim.GeoDistanceUpdaterBuilder;
import org.apache.commons.configuration2.HierarchicalConfiguration;

public class GeoDistanceUpdaterConfig implements Configurator<GeoDistanceUpdaterBuilder> {


    @Override
    public GeoDistanceUpdaterBuilder configure(HierarchicalConfiguration config) {
        return new GeoDistanceUpdaterBuilder();
    }

    @Override
    public GeoDistanceUpdaterBuilder configure(HierarchicalConfiguration config, GeoDistanceUpdaterBuilder object) {
        return object;
    }
}
