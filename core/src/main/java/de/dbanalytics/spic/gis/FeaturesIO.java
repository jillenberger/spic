/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.gis;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * @author johannes
 */
public class FeaturesIO {

    private final static Logger logger = Logger.getLogger(FeaturesIO.class);

    private GeoTransformer transformer = GeoTransformer.identityTransformer();

    public void setTransformer(GeoTransformer transformer) {
        this.transformer = transformer;
    }

    public Set<Feature> read(String filename) throws IOException {
        if (isGeoJson(filename)) {
            FeaturesIOGeoJson reader = new FeaturesIOGeoJson();
            reader.setGeoTransformer(transformer);
            return reader.read(filename);
        } else {
            String tokens[] = filename.split("\\.");
            logger.warn("Unknown file format: " + tokens[tokens.length - 1]);
            return null;
        }
    }

    public void write(Collection<? extends Feature> features, String filename) throws IOException {
        if (isGeoJson(filename)) {
            FeaturesIOGeoJson writer = new FeaturesIOGeoJson();
            writer.setGeoTransformer(transformer);
            writer.write(features, filename);
        } else {
            String tokens[] = filename.split("\\.");
            logger.warn("Unknown file format: " + tokens[tokens.length - 1]);
        }
    }

    private boolean isGeoJson(String filename) {
        String tmp = filename.toLowerCase();
        return tmp.endsWith(".json") || tmp.endsWith(".geojson");
    }
}
