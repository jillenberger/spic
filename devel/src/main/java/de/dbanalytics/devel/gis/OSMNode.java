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

package de.dbanalytics.devel.gis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 *
 */
public class OSMNode {

	private String id;
	
	private double longitude;
	
	private double latitude;
	
	private Map<String, String> tags;
	
	private boolean nodeOfWay = false; //TODO can a node be associated to multiple ways?
	
	public OSMNode(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void addTag(String key, String value) {
		if(tags == null)
			tags = new HashMap<String, String>();
		
		tags.put(key, value);
	}
	
	public Map<String, String> tags() {
		return tags;
	}
	
	public void setNodeOfWay(boolean flag) {
		nodeOfWay = flag;
	}
	
	public boolean isNodeOfWay() {
		return nodeOfWay;
	}
}
