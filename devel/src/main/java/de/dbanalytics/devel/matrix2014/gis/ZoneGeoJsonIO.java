/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.*
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.devel.matrix2014.gis;

import org.wololo.geojson.Feature;
import org.wololo.geojson.*;
import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author johannes
 * 
 */
public class ZoneGeoJsonIO {

	public static ZoneCollection readFromGeoJSON(String file, String primaryKey, String id) throws IOException {
		ZoneCollection zones = new ZoneCollection(id);
		String data = new String(Files.readAllBytes(Paths.get(file)));
		zones.addAll(ZoneGeoJsonIO.parseFeatureCollection(data));
		zones.setPrimaryKey(primaryKey);
		return zones;
	}

	public static String toJson(Zone zone) {
		GeoJSONWriter writer = new GeoJSONWriter();
		GeoJSON json = writer.write(zone.getGeometry());

		Geometry geometry = (Geometry) GeoJSONFactory.create(json.toString());
		org.wololo.geojson.Feature feature = new org.wololo.geojson.Feature(geometry, new HashMap<String, Object>(zone.attributes()));

		List<org.wololo.geojson.Feature> features = new ArrayList<>(1);
		features.add(feature);
		return writer.write(features).toString();
	}

	public static String toJson(Collection<Zone> zones) {
		GeoJSONWriter writer = new GeoJSONWriter();
		List<Feature> features = new ArrayList<>(1);

		for (Zone zone : zones) {
			GeoJSON json = writer.write(zone.getGeometry());
			Geometry geometry = (Geometry) GeoJSONFactory.create(json.toString());
			org.wololo.geojson.Feature feature = new org.wololo.geojson.Feature(geometry, new HashMap<String, Object>(zone.attributes()));
			features.add(feature);
		}

		return writer.write(features).toString();
	}

	public static Set<Zone> parseFeatureCollection(String data) {
		GeoJSON json = GeoJSONFactory.create(data);

		if (json instanceof FeatureCollection) {
			GeoJSONReader reader = new GeoJSONReader();
			Set<Zone> zones = new HashSet<>();
			FeatureCollection features = (FeatureCollection) json;
			for (org.wololo.geojson.Feature feature : features.getFeatures()) {
				Zone zone = new Zone(reader.read(feature.getGeometry()));
				for (Entry<String, Object> prop : feature.getProperties().entrySet()) {
					Object value = prop.getValue();
					if(value == null) {
						zone.setAttribute(prop.getKey(), "");
					} else {
						zone.setAttribute(prop.getKey(), prop.getValue().toString());
					}
				}
				zones.add(zone);
			}
			return zones;
		} else {
			return null;
		}

	}
}