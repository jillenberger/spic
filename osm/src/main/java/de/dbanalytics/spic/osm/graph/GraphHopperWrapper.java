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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.reader.DataReader;
import com.graphhopper.reader.osm.OSMReader;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.DataAccess;
import com.graphhopper.storage.Directory;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.BitUtil;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import de.dbanalytics.spic.util.ProgressLogger;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author johannes
 */
public class GraphHopperWrapper implements RoutingService {

    private static final Logger logger = Logger.getLogger(GraphHopperWrapper.class);

    private final AlgorithmOptions algoOpts;

    private final HintsMap hintsMap;

    private TIntObjectMap<List<Node>> ghEdge2Nodes;

    private InternalGraphHopper hopper;

    private Graph graph;

    public GraphHopperWrapper(String osmFile, String ghStorage) {
        FlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = new EncodingManager(encoder);

        hopper = new InternalGraphHopper();
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

        GraphBuilder builder = new GraphBuilder();
        graph = builder.build(osmFile);
        initEdgeMapping(graph);
    }

    private void initEdgeMapping(Graph graph) {
        logger.info("Collection nodes of osm ways...");
        TLongObjectMap<List<Node>> osmWayNodes = getOsmWayId2Nodes(graph);

        ProgressLogger plogger = new ProgressLogger(logger);
        ghEdge2Nodes = new TIntObjectHashMap<>();
        AllEdgesIterator edgesIterator = hopper.getGraphHopperStorage().getAllEdges();
        plogger.start("Mapping edges...", edgesIterator.getMaxId());

        int numOneNode = 0;
        int numNoNode = 0;

        while (edgesIterator.next()) {
            long osmWayId = hopper.getOSMWay(edgesIterator.getEdge());
            List<Node> candidates = osmWayNodes.get(osmWayId);

            if (candidates != null) {
                PointList plist = edgesIterator.fetchWayGeometry(3);

                Node startNode = getNearestNode(
                        plist.getLatitude(0),
                        plist.getLongitude(0),
                        candidates);
                Node endNode = getNearestNode(
                        plist.getLatitude(plist.size() - 1),
                        plist.getLongitude(plist.size() - 1),
                        candidates);

                int idx1 = candidates.indexOf(startNode);
                int idx2 = candidates.indexOf(endNode);

//            if(idx1 == idx2) {
//                logger.warn("Same start and end index.");
//            }

                int starIdx = Math.min(idx1, idx2);
                int endIdx = Math.max(idx1, idx2);

                List<Node> ghEdgeNodes = new ArrayList<>();
                for (int i = starIdx; i <= endIdx; i++) {
                    de.dbanalytics.spic.osm.graph.Node node = candidates.get(i);
                    if (!node.getEdges().isEmpty()) {
                        ghEdgeNodes.add(node);
                    }
                }

                if (ghEdgeNodes.size() > 0) {
                    ghEdge2Nodes.put(edgesIterator.getEdge(), ghEdgeNodes);

                    if (ghEdgeNodes.size() == 1) {
                        /**
                         * Can happen
                         * 1) if a GH-edge is only a part of an (tower-)edge. Can happen with barriers. (probably fixed)
                         * 2) with loops
                         **/
                        numOneNode++;
                    }
                } else {
                    numNoNode++;
                }
            } else {
                logger.warn(String.format("No candidates found for way %s.", osmWayId));
            }
            plogger.step();
        }
        plogger.stop();

        if (numNoNode > 0 || numOneNode > 0) {
            logger.warn(String.format("%s GH-edges with no node, %s GH-edges with only one node.", numNoNode, numOneNode));
        }
    }

    private TLongObjectMap<List<Node>> getOsmWayId2Nodes(Graph graph) {
        TLongObjectMap<SortedSet<Edge>> edges = new TLongObjectHashMap<>();

        /** collect edges for osm ways */
        for (Edge edge : graph.getEdges()) {
            SortedSet<Edge> list = edges.get(edge.getOsmWayId());
            if (list == null) {
                list = new TreeSet<>(new Comparator<Edge>() {
                    @Override
                    public int compare(Edge o1, Edge o2) {
                        return Integer.compare(o1.getOsmWayIndex(), o2.getOsmWayIndex());
                    }
                });
                edges.put(edge.getOsmWayId(), list);
            }

            list.add(edge);
        }

        /** build node sequence for osm way */
        TLongObjectMap<List<Node>> nodes = new TLongObjectHashMap<>(edges.size());
        TLongObjectIterator<SortedSet<Edge>> it = edges.iterator();
        for (int i = 0; i < edges.size(); i++) {
            it.advance();

            SortedSet<Edge> list = it.value();
            List<Node> path = new ArrayList<>();
            path.add(list.first().getFrom());

            for (Edge edge : list) {
                path.addAll(edge.getBends());
                path.add(edge.getTo());
            }

            nodes.put(it.key(), path);
        }

        return nodes;
    }


    public Graph getGraph() {
        return graph;
    }

    private de.dbanalytics.spic.osm.graph.Node getNearestNode(double lat, double lon, List<de.dbanalytics.spic.osm.graph.Node> nodes) {
        double d_min = Double.MAX_VALUE;
        de.dbanalytics.spic.osm.graph.Node theNode = null;
        for (de.dbanalytics.spic.osm.graph.Node node : nodes) {
            double dx = lon - node.getLongitude();
            double dy = lat - node.getLatitude();
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d < d_min) {
                d_min = d;
                theNode = node;
            }
        }

        return theNode;
    }

    public GhRoute query(double fromLat, double fromLon, double toLat, double toLon) {
        LocationIndex index = hopper.getLocationIndex();
        GraphHopperStorage graph = hopper.getGraphHopperStorage();

        QueryResult fromQR = index.findClosest(fromLat, fromLon, EdgeFilter.ALL_EDGES);
        QueryResult toQR = index.findClosest(toLat, toLon, EdgeFilter.ALL_EDGES);

        if (!fromQR.isValid() || !toQR.isValid()) return null;

//        QueryGraph queryGraph = new QueryGraph(graph.getGraph(CHGraphImpl.class));
//        queryGraph.lookup(fromQR, toQR);

        com.graphhopper.storage.Graph queryGraph = graph.getGraph(CHGraphImpl.class);
        RoutingAlgorithm algorithm = hopper.getAlgorithmFactory(hintsMap).createAlgo(queryGraph, algoOpts);

        int fromNodeId = fromQR.getClosestNode();
        int toNodeId = toQR.getClosestNode();
        Path path = algorithm.calcPath(fromNodeId, toNodeId);

        if (path.isFound()) {
            return new GhRoute(path, ghEdge2Nodes);
        } else {
            return null;
        }
    }

    private static class InternalGraphHopper extends GraphHopper {

        // mapping of internal edge ID to OSM way ID
        private DataAccess edgeMapping;
        private BitUtil bitUtil;

        @Override
        public boolean load(String graphHopperFolder) {
            boolean loaded = super.load(graphHopperFolder);

            Directory dir = getGraphHopperStorage().getDirectory();
            bitUtil = BitUtil.get(dir.getByteOrder());
            edgeMapping = dir.find("edge_mapping");

            if (loaded) {
                edgeMapping.loadExisting();
            }

            return loaded;
        }

        @Override
        protected DataReader createReader(GraphHopperStorage ghStorage) {
            OSMReader reader = new OSMReader(ghStorage) {

                {
                    edgeMapping.create(1000);
                }

                // this method is only in >0.6 protected, before it was private
                @Override
                protected void storeOsmWayID(int edgeId, long osmWayId) {
                    super.storeOsmWayID(edgeId, osmWayId);

                    long pointer = 8L * edgeId;
                    edgeMapping.ensureCapacity(pointer + 8L);

                    edgeMapping.setInt(pointer, bitUtil.getIntLow(osmWayId));
                    edgeMapping.setInt(pointer + 4, bitUtil.getIntHigh(osmWayId));
                }

                @Override
                protected void finishedReading() {
                    super.finishedReading();

                    edgeMapping.flush();
                }
            };

            return initDataReader(reader);
        }

        public long getOSMWay(int internalEdgeId) {
            long pointer = 8L * internalEdgeId;
            return bitUtil.combineIntsToLong(edgeMapping.getInt(pointer), edgeMapping.getInt(pointer + 4L));
        }

        @Override
        public List<Path> calcPaths(GHRequest request, GHResponse rsp) {
            return super.calcPaths(request, rsp);
        }
    }
}
