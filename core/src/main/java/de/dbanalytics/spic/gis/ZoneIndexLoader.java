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

    @Override
    public Object load(String filename) {
        try {
            logger.info("Loading zones...");
            FeaturesIO featuresIO = new FeaturesIO();
            featuresIO.setTransformer(GeoTransformer.WGS84toX(epsg));
            Set<Feature> features = featuresIO.read(filename);
            logger.info(String.format("Loaded %s zones.", features.size()));

            return new ZoneIndex(features);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
