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

package de.dbanalytics.spic.osm.graph;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.CarFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.util.PointList;
import de.dbanalytics.spic.util.ProgressLogger;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 */
public class MyImport {

    private static final Logger logger = Logger.getLogger(MyImport.class);

    public static void main(String[] args) throws IOException {
        String osmFile = args[0];
        String ghStorage = args[1];
        String mappingFile = args[2];


        FlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = new EncodingManager(encoder);

        MyGraphHopper hopper = new MyGraphHopper();
//        hopper.setInMemory();
        hopper.setDataReaderFile(osmFile);
        hopper.setGraphHopperLocation(ghStorage);
        hopper.setEncodingManager(em);
        hopper.setCHEnabled(true);
        hopper.importOrLoad();


        /**
         * Map OSM-way to OSM-nodes
         */
        logger.info("Collecting nodes...");
        TLongObjectMap<Node> allNodes = new TLongObjectHashMap<>();
        OsmIterator it = getOsmIterator(osmFile);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                OsmNode node = (OsmNode) container.getEntity();


                allNodes.put(node.getId(), new Node(node.getId(), node.getLatitude(), node.getLongitude()));
            }
        }
        /**
         * Collect nodes part of a highway.
         */
        logger.info("Filtering for nodes of highways...");
//        TLongObjectMap<Point> hwNodes = new TLongObjectHashMap<>();
        TLongObjectMap<ArrayList<Node>> edge2nodes = new TLongObjectHashMap<>();
        it = getOsmIterator(osmFile);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();
                if (OsmModelUtil.getTagsAsMap(way).containsKey("highway")) {
                    ArrayList<Node> nodes = new ArrayList<>(way.getNumberOfNodes());
                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long id = way.getNodeId(i);
                        Node node = allNodes.get(id);
                        nodes.add(node);
                    }

                    edge2nodes.put(way.getId(), nodes);
                }
            }
        }
        /**
         * Go through all edges
         */
        ProgressLogger plogger = new ProgressLogger(logger);

        BufferedWriter writer = new BufferedWriter(new FileWriter(mappingFile));
        writer.write("GHId\tOSMfrom\tOSMto");
        writer.newLine();
        TIntObjectMap<long[]> edge2OsmNode = new TIntObjectHashMap<>();
        AllEdgesIterator edgesIterator = hopper.getGraphHopperStorage().getAllEdges();
        plogger.start("Mapping edges...", edgesIterator.getMaxId());
        while (edgesIterator.next()) {
            long osmWayId = hopper.getOSMWay(edgesIterator.getEdge());
            List<Node> nodes = edge2nodes.get(osmWayId);
            TLongArrayList ghEdgeNodes = new TLongArrayList();
            PointList plist = edgesIterator.fetchWayGeometry(3);

            double fromLat = plist.getLatitude(0);
            double fromLon = plist.getLongitude(0);
            long fromOsmNode = getNearestNode(fromLat, fromLon, nodes);
            ghEdgeNodes.add(fromOsmNode);

            double toLat = plist.getLatitude(plist.getSize() - 1);
            double toLon = plist.getLongitude(plist.getSize() - 1);
            long toOsmNode = getNearestNode(toLat, toLon, nodes);
            ghEdgeNodes.add(toOsmNode);

//            edge2OsmNode.put(edgesIterator.getEdge(), ghEdgeNodes.toArray());

            writer.write(String.valueOf(edgesIterator.getEdge()));
            writer.write("\t");
            writer.write(String.valueOf(fromOsmNode));
            writer.write("\t");
            writer.write(String.valueOf(toOsmNode));
            writer.newLine();
            plogger.step();
        }
        writer.close();
        plogger.stop();
    }

    private static long getNearestNode(double lat, double lon, List<Node> nodes) {
        double d_min = Double.MAX_VALUE;
        Node theNode = null;
        for (Node node : nodes) {
            double dx = lon - node.lon;
            double dy = lat - node.lat;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < d_min) {
                d_min = d;
                theNode = node;
            }
        }

        return theNode.id;
    }

    private static OsmIterator getOsmIterator(String file) throws FileNotFoundException {
        if (file.endsWith(".xml") || file.endsWith(".osm")) {
            return new OsmXmlIterator(new FileInputStream(file), false);
        } else if (file.endsWith(".pbf")) {
            return new PbfIterator(new FileInputStream(file), false);
        } else {
            throw new RuntimeException("File format unknown. Can only parse .xml, .osm and .pbf.");
        }
    }

    private static class Node {

        long id;

        double lat;

        double lon;

        public Node(long id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }
    }
}
