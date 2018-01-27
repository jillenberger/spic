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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author johannes
 *
 */
public class OSMWay {

	private String id;
	
	private List<OSMNode> nodes;
	
	private Map<String, String> tags;
	
	public OSMWay(String id) {
		this.id = id;
		this.nodes = new ArrayList<OSMNode>();
		this.tags = new HashMap<String, String>();
	}
	
	public String getId() {
		return id;
	}
	
	public void addNode(OSMNode node) {
		nodes.add(node);
	}
	
	public List<OSMNode> getNodes() {
		return nodes;
	}
	
	public void addTag(String key, String value) {
		tags.put(key, value);
	}
	
	public Map<String, String> tags() {
		return tags;
	}
}
