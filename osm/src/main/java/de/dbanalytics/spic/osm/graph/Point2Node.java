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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.list.array.TLongArrayList;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author johannes
 */
public class Point2Node {

    private static final Logger logger = Logger.getLogger(Point2Node.class);

    private static final int ROUND_FACTOR_LON = 10000000;

    private static final int ROUND_FACTOR_LAT = 1000000;

    private final double[] longitudes;

    private final LonEntry[] lonEntryIndex;

    public Point2Node(InputStream stream) throws IOException {
        OsmIterator it = new OsmXmlIterator(stream, false);

        SortedMap<Double, LonEntry> lonEntryMap = new TreeMap<>();

        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                OsmNode node = (OsmNode) container.getEntity();
                double lon = roundLon(node.getLongitude());

                LonEntry entry = lonEntryMap.get(lon);
                if (entry == null) {
                    entry = new LonEntry();
                    lonEntryMap.put(lon, entry);
                }

                entry.addNode(node);
            }
        }

        longitudes = new double[lonEntryMap.size()];
        int idx = 0;
        for (Double key : lonEntryMap.keySet()) {
            longitudes[idx] = key;
            idx++;
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/johannes/Desktop/longitudes.txt"));
        for (double lon : longitudes) {
            writer.write(String.valueOf(lon));
            writer.newLine();
        }
        writer.close();

        lonEntryIndex = new LonEntry[lonEntryMap.size()];
        idx = 0;
        for (LonEntry entry : lonEntryMap.values()) {
            entry.optimize();
            lonEntryIndex[idx] = entry;
            idx++;
        }

    }

    public static void main(String args[]) throws IOException {
        String osmFile = "/Users/johannes/Desktop/bockenheim.osm";

        FlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = new EncodingManager(encoder);

        GraphHopper hopper = new GraphHopperOSM().forDesktop();
        hopper.setDataReaderFile(osmFile);
        hopper.setGraphHopperLocation("/Users/johannes/Desktop/bockenheim-gh/");
        hopper.setEncodingManager(em);
        hopper.setCHEnabled(true);
        hopper.importOrLoad();

        GraphHopperStorage graph = hopper.getGraphHopperStorage();

        LocationIndex index = hopper.getLocationIndex();

        AlgorithmOptions algoOpts = AlgorithmOptions.start().algorithm(Parameters.Algorithms.ASTAR_BI).
                traversalMode(TraversalMode.NODE_BASED).
                weighting(new FastestWeighting(encoder)).
                build();

        QueryResult toQR = index.findClosest(50.1264737, 8.65256, EdgeFilter.ALL_EDGES);
        QueryResult fromQR = index.findClosest(50.1209992, 8.6439189, EdgeFilter.ALL_EDGES);

        QueryGraph queryGraph = new QueryGraph(graph.getGraph(CHGraphImpl.class));
        queryGraph.lookup(fromQR, toQR);

        HintsMap hintsMap = new HintsMap();
        hintsMap.setVehicle("car");
        hintsMap.setWeighting("fastest");

        RoutingAlgorithm algorithm = hopper.getAlgorithmFactory(hintsMap).createAlgo(queryGraph, algoOpts);

        Path path = algorithm.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());

        Point2Node point2Node = new Point2Node(new FileInputStream(osmFile));


        PointList plist = path.calcPoints();

        TLongArrayList osmIds = new TLongArrayList(plist.size());
        for (GHPoint p : plist) {
//            double lon = round(p.getLon());
//            double lat = round(p.getLat());
            long id = point2Node.getNode(p.getLon(), p.getLat());
            osmIds.add(id);
        }

        logger.info(osmIds);
    }

    public long getNode(double longitude, double latitude) {
        longitude = roundLon(longitude);
        latitude = roundLat(latitude);

        int idx = Arrays.binarySearch(longitudes, longitude);
        if (idx >= 0) {
            LonEntry entry = lonEntryIndex[idx];
            return entry.getNode(latitude);
        } else {
            return -1;
        }
    }

    private double roundLat(double value) {
        return Math.ceil(value * ROUND_FACTOR_LAT) / (double) ROUND_FACTOR_LAT;
    }

    private double roundLon(double value) {
        return Math.ceil(value * ROUND_FACTOR_LON) / (double) ROUND_FACTOR_LON;
    }

    private class LonEntry {

        private double[] latitudes;

        private long[] nodeIndex;

        private SortedMap<Double, Long> nodeMap = new TreeMap<>();

        public void addNode(OsmNode node) {
            double lat = roundLat(node.getLatitude());
            if (!nodeMap.containsKey(lat)) {
                nodeMap.put(lat, node.getId());
            } else {
                logger.warn(String.format("Overwriting entry for node %s (%s %s).",
                        node.getId(),
                        node.getLongitude(),
                        node.getLatitude()));
            }
        }

        public void optimize() {
            latitudes = new double[nodeMap.size()];
            int idx = 0;
            for (Double key : nodeMap.keySet()) {
                latitudes[idx] = key;
                idx++;
            }

            nodeIndex = new long[nodeMap.size()];
            idx = 0;
            for (Long id : nodeMap.values()) {
                nodeIndex[idx] = id;
                idx++;
            }

            nodeMap = null;
        }

        public long getNode(double latitude) {
            if (nodeIndex.length == 0) return -1; // TODO: Can this happen?
            else if (nodeIndex.length == 1) return nodeIndex[0];
            else {
                int idx = Arrays.binarySearch(latitudes, latitude);
                if (idx >= 0) {
                    return nodeIndex[idx];
                } else {
                    return -1;
                }
            }
        }
    }
}
