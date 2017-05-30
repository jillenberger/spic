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

import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Geometry;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.util.*;

/**
 * Created by johannesillenberger on 30.05.17.
 */
public class FeaturesIOGeoJson {

    private GeoTransformer transformer;

    public Set<Feature> read(String filename) {
        return null;
    }

    public void write(Collection<? extends Feature> features, String filename) {
        GeoJSONWriter jsonWriter = new GeoJSONWriter();
        List<org.wololo.geojson.Feature> jsonFeatures = new ArrayList<>(features.size());

        for (Feature feature : features) {
            com.vividsolutions.jts.geom.Geometry geometry = (com.vividsolutions.jts.geom.Geometry) feature.getGeometry().clone();
            transformer.backward(geometry);

            GeoJSON tmpData = jsonWriter.write(geometry);
            Geometry jsonGeometry = (Geometry) GeoJSONFactory.create(tmpData.toString());
            org.wololo.geojson.Feature jsonFeature = new org.wololo.geojson.Feature(jsonGeometry, new HashMap<String, Object>(feature.getAttributes()));
            jsonFeatures.add(jsonFeature);
        }

        String data = jsonWriter.write(jsonFeatures).toString();
    }
}
