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
import com.vividsolutions.jts.geom.Polygonal;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 * 
 */
public class Zone {

	private static final Logger logger = Logger.getLogger(Zone.class);
	
	private final Geometry geometry;

	private Map<String, String> attributes;

	public Zone(Geometry geometry) {
		if(!(geometry instanceof Polygonal))
			logger.warn("Geometry is not instance of Polygonal. This is ok but may have effects on geometric operations.");
		
		this.geometry = geometry;
	}

	public Geometry getGeometry() {
		return geometry;
	}
	
	private void initAttributes() {
		if (attributes == null)
			attributes = new HashMap<String, String>();
	}

	public String getAttribute(String key) {
		if (attributes == null)
			return null;
		else
			return attributes.get(key);
	}

	public Map<String, String> attributes() {
		return Collections.unmodifiableMap(attributes);
	}
	
	public String setAttribute(String key, String value) {
		initAttributes();
		return attributes.put(key, value);
	}

	public String removeAttribute(String key) {
		if(attributes == null) return null;
		else return attributes.remove(key);
	}
}
