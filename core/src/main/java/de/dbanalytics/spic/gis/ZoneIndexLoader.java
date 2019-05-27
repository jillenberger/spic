package de.dbanalytics.spic.gis;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;

public class ZoneIndexLoader implements DataLoader {

    private static final Logger logger = Logger.getLogger(ZoneIndexLoader.class);

    private final int epsg;

    public ZoneIndexLoader(int epsg) {
        this.epsg = epsg;
    }

    public ZoneIndexLoader() {
        epsg = 0;
    }

    @Override
    public Object load(String filename) {
        try {
            FeaturesIO featuresIO = new FeaturesIO();
            if(epsg > 0) {
                featuresIO.setTransformer(GeoTransformer.WGS84toX(epsg));
            }
            Set<Feature> features = featuresIO.read(filename);

            return new ZoneIndex(features);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
