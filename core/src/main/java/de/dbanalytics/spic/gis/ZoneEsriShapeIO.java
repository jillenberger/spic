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

package de.dbanalytics.spic.gis;

import com.vividsolutions.jts.geom.Geometry;
import org.matsim.contrib.common.gis.EsriShapeIO;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 *
 */
public class ZoneEsriShapeIO {

	public static ZoneCollection read(String filename, String primaryKey) {
		ZoneCollection zones = read(filename);
		zones.setPrimaryKey(primaryKey);
		return zones;
	}

	public static ZoneCollection read(String filename) {
		ZoneCollection zones = new ZoneCollection(null);
		Set<Zone> zoneSet = new HashSet<>();
		
		try {
			for(SimpleFeature feature : EsriShapeIO.readFeatures(filename)) {
				Zone zone = new Zone((Geometry) feature.getDefaultGeometry());
		
				for(Property prop : feature.getProperties()) {
					String name = prop.getName().getLocalPart();
					Object valueObj = prop.getValue();
					String value = null;
					if(valueObj != null) value = valueObj.toString();
					zone.setAttribute(name, value);
					
				}
				
				zoneSet.add(zone);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		zones.addAll(zoneSet);
		
		return zones;
	}
}
