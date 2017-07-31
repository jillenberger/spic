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
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author johannes
 */
public class GraphHopperWrapper {

    private static final Logger logger = Logger.getLogger(GraphHopperWrapper.class);

    private final AlgorithmOptions algoOpts;

    private final HintsMap hintsMap;

    private TIntObjectMap<long[]> edge2osmPair;

    private MyGraphHopper hopper;

    private TowerNodeNetwork towerNodeNetwork;

    private TLongObjectMap<TowerNodeNetwork.Node> towers;

    public GraphHopperWrapper(String osmFile, String ghStorage) {
        FlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = new EncodingManager(encoder);

        hopper = new MyGraphHopper();
        hopper.setDataReaderFile(osmFile);
        hopper.setGraphHopperLocation(ghStorage);
        hopper.setEncodingManager(em);
        hopper.setCHEnabled(true);
        hopper.importOrLoad();

        algoOpts = AlgorithmOptions.start().algorithm(Parameters.Algorithms.ASTAR_BI).
                traversalMode(TraversalMode.NODE_BASED).
                weighting(new FastestWeighting(encoder)).
                build();

        hintsMap = new HintsMap();
        hintsMap.setVehicle("car");
        hintsMap.setWeighting("fastest");

        try {
            initEdgeMapping(osmFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initEdgeMapping(String osmFile) throws FileNotFoundException {
        towerNodeNetwork = new TowerNodeNetwork(osmFile);
        towers = new TLongObjectHashMap<>();
        for (TowerNodeNetwork.Edge edge : towerNodeNetwork.getEdges()) {
            towers.put(edge.getFrom().getId(), edge.getFrom());
            towers.put(edge.getTo().getId(), edge.getTo());
        }
        long[] towerIds = towers.keys();
        Arrays.sort(towerIds);
        /**
         * Map OSM-way to OSM-nodes
         */
        logger.info("Collecting nodes...");
        TLongObjectMap<Node> allOsmNodes = new TLongObjectHashMap<>();
        OsmIterator it = getOsmIterator(osmFile);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                OsmNode node = (OsmNode) container.getEntity();


                allOsmNodes.put(node.getId(), new Node(node.getId(), node.getLatitude(), node.getLongitude()));
            }
        }
        /**
         * Collect nodes part of a highway.
         */
        logger.info("Filtering for nodes of highways...");
        TLongObjectMap<ArrayList<Node>> osmEdge2OsmNodes = new TLongObjectHashMap<>();
        it = getOsmIterator(osmFile);
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();
                if (OsmModelUtil.getTagsAsMap(way).containsKey("highway")) {
                    ArrayList<Node> nodes = new ArrayList<>(way.getNumberOfNodes());
                    for (int i = 0; i < way.getNumberOfNodes(); i++) {
                        long id = way.getNodeId(i);
                        Node node = allOsmNodes.get(id);
                        nodes.add(node);
                    }

                    osmEdge2OsmNodes.put(way.getId(), nodes);
                }
            }
        }
        logger.info("Done.");
        /**
         * Go through all edges
         */
        ProgressLogger plogger = new ProgressLogger(logger);
        edge2osmPair = new TIntObjectHashMap<>();
        AllEdgesIterator edgesIterator = hopper.getGraphHopperStorage().getAllEdges();
        plogger.start("Mapping edges...", edgesIterator.getMaxId());
        while (edgesIterator.next()) {
            long osmWayId = hopper.getOSMWay(edgesIterator.getEdge());
            List<Node> osmNodes = osmEdge2OsmNodes.get(osmWayId);
            TLongArrayList ghNodes = new TLongArrayList();
            PointList plist = edgesIterator.fetchWayGeometry(3);

            Node startNode = getNearestNode(plist.getLatitude(0), plist.getLongitude(0), osmNodes);
            Node endNode = getNearestNode(plist.getLatitude(plist.size() - 1), plist.getLongitude(plist.size() - 1), osmNodes);

            int idx1 = osmNodes.indexOf(startNode);
            int idx2 = osmNodes.indexOf(endNode);

            int starIdx = Math.min(idx1, idx2);
            int endIdx = Math.max(idx1, idx2);

            for (int i = starIdx; i <= endIdx; i++) {
                Node node = osmNodes.get(i);
                if (Arrays.binarySearch(towerIds, node.id) >= 0) {
                    ghNodes.add(node.id);
                }
            }

            if (ghNodes.size() > 0) {
                edge2osmPair.put(edgesIterator.getEdge(), ghNodes.toArray());
            }


            plogger.step();
        }

        plogger.stop();
    }

    public BBox getBoundingBox() {
        return hopper.getGraphHopperStorage().getBounds();
    }

    public TowerNodeNetwork getTowerNodeNetwork() {
        return towerNodeNetwork;
    }

    private Node getNearestNode(double lat, double lon, List<Node> nodes) {
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

        return theNode;
    }

    public TLongArrayList query(double fromLat, double fromLon, double toLat, double toLon) {
        LocationIndex index = hopper.getLocationIndex();
        GraphHopperStorage graph = hopper.getGraphHopperStorage();

        QueryResult fromQR = index.findClosest(fromLat, fromLon, EdgeFilter.ALL_EDGES);
        QueryResult toQR = index.findClosest(toLat, toLon, EdgeFilter.ALL_EDGES);

        if (!fromQR.isValid() || !toQR.isValid()) return null;

        QueryGraph queryGraph = new QueryGraph(graph.getGraph(CHGraphImpl.class));
        queryGraph.lookup(fromQR, toQR);

        RoutingAlgorithm algorithm = hopper.getAlgorithmFactory(hintsMap).createAlgo(queryGraph, algoOpts);

        int fromNodeId = fromQR.getClosestNode();
        int toNodeId = toQR.getClosestNode();
        Path path = algorithm.calcPath(fromNodeId, toNodeId);

        if (path.isFound()) {
            TLongArrayList nodes = new TLongArrayList(path.getEdgeCount() + 1);
            List<EdgeIteratorState> ghEdges = path.calcEdges();
            List<long[]> osmEdges = new ArrayList<>(ghEdges.size());
            for (EdgeIteratorState ghEdge : ghEdges) {
                long[] osmEdge = edge2osmPair.get(ghEdge.getEdge());
                if (osmEdge != null) {
                    osmEdges.add(osmEdge);
                }
            }

            if (osmEdges.size() == 0) {
                return nodes;
            } else {
                if (osmEdges.size() == 1) {
                    long[] edgeNodes = osmEdges.get(0);
                    nodes.addAll(edgeNodes);
                } else {
                    long[] firstEdge = osmEdges.get(0);
                    long[] secondEdge = osmEdges.get(1);

                    if (firstEdge[firstEdge.length - 1] == secondEdge[0]) {
                        /** both edges correct order */
                        nodes.addAll(firstEdge);
                    } else if (firstEdge[0] == secondEdge[0]) {
                        /** fist edge reversed  - second correct */
                        for (int k = firstEdge.length - 1; k >= 0; k--) {
                            nodes.add(firstEdge[k]);
                        }
                    } else if (firstEdge[0] == secondEdge[secondEdge.length - 1]) {
                        /** both edges reversed */
                        for (int k = firstEdge.length - 1; k >= 0; k--) {
                            nodes.add(firstEdge[k]);
                        }
                    } else if (firstEdge[firstEdge.length - 1] == secondEdge[secondEdge.length - 1]) {
                        /** second edge reversed */
                        nodes.addAll(firstEdge);
                    } else {
                        logger.warn(String.format("Non consecutive edges: %s - %s",
                                Arrays.toString(firstEdge),
                                Arrays.toString(secondEdge)));
                        return null;
                    }

                    for (int i = 1; i < osmEdges.size(); i++) {
                        long[] edgeNodes = osmEdges.get(i);
                        long last = nodes.get(nodes.size() - 1);
                        if (last == edgeNodes[0]) {
                            /** correct order */
                            for (int k = 1; k < edgeNodes.length; k++) {
                                nodes.add(edgeNodes[k]);
                            }
                        } else if (last == edgeNodes[edgeNodes.length - 1]) {
                            /** reverse order */
                            for (int k = edgeNodes.length - 2; k >= 0; k--) {
                                nodes.add(edgeNodes[k]);
                            }
                        }
                    }
                }
            }
            return nodes;
        } else {
            return null;
        }
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
