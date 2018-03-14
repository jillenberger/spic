/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.gis;

import de.dbanalytics.spic.util.ProgressLogger;
import org.apache.log4j.Logger;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Geometry;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by johannesillenberger on 30.05.17.
 */
public class FeaturesIOGeoJson {

    private static final Logger logger = Logger.getLogger(FeaturesIOGeoJson.class);

    private GeoTransformer transformer = GeoTransformer.identityTransformer();

    private boolean verbose = true;

    public void setGeoTransformer(GeoTransformer transformer) {
        this.transformer = transformer;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public Set<Feature> read(String filename) throws IOException {
        String data = new String(Files.readAllBytes(Paths.get(filename)));
        GeoJSON jsonData = GeoJSONFactory.create(data);

        if (jsonData instanceof FeatureCollection) {
            long time = System.currentTimeMillis();
            ProgressLogger progressLogger = new ProgressLogger(logger);

            GeoJSONReader reader = new GeoJSONReader();
            Set<Feature> features = new LinkedHashSet<>();
            FeatureCollection jsonFeatures = (FeatureCollection) jsonData;

            if (verbose) progressLogger.start("Loading features", jsonFeatures.getFeatures().length);
            for (org.wololo.geojson.Feature jsonFeature : jsonFeatures.getFeatures()) {
                Map<String, String> attributes = new HashMap<>();
                for (Map.Entry<String, Object> prop : jsonFeature.getProperties().entrySet()) {
                    Object value = prop.getValue();
                    //TODO: Should attribute keys be always lower case?
                    if (value != null) attributes.put(prop.getKey().toLowerCase(), prop.getValue().toString());
                }

                Feature feature = new Feature(attributes.get("id"), reader.read(jsonFeature.getGeometry()));
                attributes.remove("id");
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    feature.setAttribute(entry.getKey(), entry.getValue().toString());
                }
                transformer.forward(feature.getGeometry());
                features.add(feature);

                if (verbose) progressLogger.step();
            }

            if (verbose) {
                progressLogger.stop(String.format(
                        Locale.US,
                        "%s features in %.2f secs",
                        features.size(),
                        (System.currentTimeMillis() - time) / 1000.0));
            }
            return features;
        } else {
            logger.error("JSON type is not a feature collection.");
            return null;
        }
    }

    public void write(Collection<? extends Feature> features, String filename) throws IOException {
        GeoJSONWriter jsonWriter = new GeoJSONWriter();
        List<org.wololo.geojson.Feature> jsonFeatures = new ArrayList<>(features.size());

        for (Feature feature : features) {
            com.vividsolutions.jts.geom.Geometry geometry = (com.vividsolutions.jts.geom.Geometry) feature.getGeometry().clone();
            transformer.backward(geometry);

            GeoJSON tmpData = jsonWriter.write(geometry);
            Geometry jsonGeometry = (Geometry) GeoJSONFactory.create(tmpData.toString());
            org.wololo.geojson.Feature jsonFeature = new org.wololo.geojson.Feature(
                    feature.getId(),
                    jsonGeometry,
                    new HashMap<>(feature.getAttributes()));

            jsonFeatures.add(jsonFeature);
        }

        FeatureCollection data = jsonWriter.write(jsonFeatures);
        Files.write(Paths.get(filename), data.toString().getBytes());
    }
}
