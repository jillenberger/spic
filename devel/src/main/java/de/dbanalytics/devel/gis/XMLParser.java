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

import org.apache.log4j.Logger;
import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author johannes
 * 
 */
public class XMLParser extends MatsimXmlParser {

	private static final Logger logger = Logger.getLogger(XMLParser.class);

	private static final String NODE_TAG = "node";

	private static final String ID_KEY = "id";

	private static final String LAT_KEY = "lat";

	private static final String LON_KEY = "lon";

	private static final String WAY_TAG = "way";

	private static final String ND_TAG = "nd";

	private static final String REF_KEY = "ref";

	private static final String TAG_TAG = "tag";

	private static final String KEY_KEY = "k";

	private static final String VALUE_KEY = "v";

	private Map<String, OSMNode> nodes = new HashMap<String, OSMNode>();

	private Map<String, OSMWay> ways = new HashMap<String, OSMWay>();

	private OSMWay activeWay;

	private OSMNode activeNode;

	private int nodesNotFound;

	public Map<String, OSMNode> getNodes() {
		return nodes;
	}

	public Map<String, OSMWay> getWays() {
		return ways;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.core.utils.io.MatsimXmlParser#startTag(java.lang.String,
	 * org.xml.sax.Attributes, java.util.Stack)
	 */
	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {
		if (name.equalsIgnoreCase(NODE_TAG)) {
			OSMNode node = new OSMNode(atts.getValue(ID_KEY));
			double x = Double.parseDouble(atts.getValue(LON_KEY));
			double y = Double.parseDouble(atts.getValue(LAT_KEY));
			node.setLongitude(x);
			node.setLatitude(y);

			activeNode = node;

			nodes.put(node.getId(), node);

			if (nodes.size() % 1000000 == 0) {
				logger.debug(String.format("Loaded %s nodes.", nodes.size()));
			}

		} else if (name.equalsIgnoreCase(WAY_TAG)) {
			OSMWay way = new OSMWay(atts.getValue(ID_KEY));
			activeWay = way;
		} else if (name.equalsIgnoreCase(ND_TAG)) {
			if (activeWay != null) {
				OSMNode node = nodes.get(atts.getValue(REF_KEY));
				if (node == null) {
					logger.trace(String.format("Node with id=%s not found.", atts.getValue(REF_KEY)));
					nodesNotFound++;
				} else {
					activeWay.addNode(node);
					node.setNodeOfWay(true);
				}
			}
		} else if (name.equalsIgnoreCase(TAG_TAG)) {
			if (activeWay != null) {
				String key = atts.getValue(KEY_KEY);
				String val = atts.getValue(VALUE_KEY);
				activeWay.addTag(key, val);

			} else if (activeNode != null) {
				String key = atts.getValue(KEY_KEY);
				String val = atts.getValue(VALUE_KEY);
				activeNode.addTag(key, val);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.matsim.core.utils.io.MatsimXmlParser#endTag(java.lang.String,
	 * java.lang.String, java.util.Stack)
	 */
	@Override
	public void endTag(String name, String content, Stack<String> context) {
		if (name.equalsIgnoreCase(WAY_TAG)) {
			ways.put(activeWay.getId(), activeWay);
			activeWay = null;

			if (ways.size() % 100000 == 0) {
				logger.debug(String.format("Loaded %s ways, %s node references not found.", ways.size(), nodesNotFound));
			}
		} else if (name.equalsIgnoreCase(NODE_TAG)) {
			activeNode = null;
		}

	}

}
