/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.devel.osm;

import com.graphhopper.coll.LongIntMap;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.storage.GraphHopperStorage;

/**
 * @author johannes
 */
public class GraphHopperWrapper extends GraphHopperOSM {

    private LongIntMap nodeMap;

    private int towerNode;

    private int empty;

    @Override
    protected DataReader createReader(GraphHopperStorage ghStorage) {
        OSMReaderWrapper wrapper = new OSMReaderWrapper(ghStorage);
        towerNode = wrapper.getTowerNode();
        empty = wrapper.getEmpty();
        return initDataReader(wrapper);
    }

    public int getGHNodeId(long osmId) {
        int id = nodeMap.get(osmId);
        if (id < towerNode)
            return -id - 3;

        return empty;

    }
    private class OSMReaderWrapper extends OSMReader {

        public OSMReaderWrapper(GraphHopperStorage ghStorage) {
            super(ghStorage);
        }

        @Override
        protected void finishedReading() {
            nodeMap = getNodeMap();
            super.finishedReading();
        }

        private int getTowerNode() { return OSMReader.TOWER_NODE; }

        private int getEmpty() { return OSMReader.EMPTY; }
    }
}
