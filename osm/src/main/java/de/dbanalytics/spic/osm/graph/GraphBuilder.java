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

package de.dbanalytics.spic.osm.graph;

import de.dbanalytics.spic.util.ProgressLogger;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author jillenberger
 */
public class GraphBuilder {

    private static final Logger logger = Logger.getLogger(GraphBuilder.class);

    private TLongArrayList endNodes;

    private static final double NODE_MERGE_THRESHOLD = 0.0000004;

    public Graph build(String osmFile) {
        endNodes = new TLongArrayList();
        Graph graph = buildFullNetwork(osmFile);
        graph = buildeTowerNetwork(graph);
        graph = mergeCloseNodes(graph);
        return graph;
    }

    private Graph buildeTowerNetwork(Graph graph) {
        /** get pillar (bend) nodes */
        logger.info("Collecting pillar nodes...");
        endNodes.sort();
        List<Node> pillars = new LinkedList<>();
        for (Node node : graph.getNodes()) {
            if (node.getEdges().size() == 2 && endNodes.binarySearch(node.getId()) < 0) {
                pillars.add(node);
            }
        }
        logger.info(String.format("Collected %s pillar nodes.", pillars.size()));

        ProgressLogger pLogger = new ProgressLogger(logger);
        pLogger.start("Simplifying network...", pillars.size());
//        int cnt = 0;
        for (Node pillar : pillars) {
            Edge edge1 = pillar.getEdges().get(0);
            Edge edge2 = pillar.getEdges().get(1);

            Node newFrom = null;
            Node newTo = null;
            Edge first = null;
            Edge second = null;

            if (edge1.getFrom() == pillar) {
                newFrom = edge2.getFrom();
                newTo = edge1.getTo();
                first = edge2;
                second = edge1;

            } else if (edge1.getTo() == pillar) {
                newFrom = edge1.getFrom();
                newTo = edge2.getTo();
                first = edge1;
                second = edge2;

            } else {
                throw new RuntimeException("Incorrect direction of edges.");
            }

            Edge newEdge = new Edge(newFrom, newTo, first.getOsmWayId(), first.getOsmWayIndex());
            newEdge.getBends().addAll(first.getBends());
            newEdge.getBends().add(pillar);
            newEdge.getBends().addAll(second.getBends());

            graph.removeEdge(edge1);
            graph.removeEdge(edge2);
            graph.removeNode(pillar);

            graph.addEdge(newEdge);

            pLogger.step();
//            cnt++;
//            if(cnt % 1000 == 0) logger.info(String.format("Processed %s nodes...", cnt));
        }
        pLogger.stop();

        logger.info(String.format("Simplified network: %s nodes, %s edges.", graph.getNodes().size(), graph.getEdges().size()));
        return graph;
    }

    private Graph buildFullNetwork(String osmFile) {
        try {
            logger.info("Loading nodes...");
            InputStream osmStream = new FileInputStream(osmFile);

            OsmIterator it = new OsmXmlIterator(osmStream, false);
            TLongObjectMap<OsmNode> osmNodes = new TLongObjectHashMap<>();
            long numEntities = 0;
            for (EntityContainer container : it) {
                if (container.getType() == EntityType.Node) {
                    OsmNode osmNode = (OsmNode) container.getEntity();
                    osmNodes.put(osmNode.getId(), (OsmNode) container.getEntity());
                }
                numEntities++;
            }
            logger.info(String.format("Loaded %s OSM nodes.", osmNodes.size()));

            Graph graph = new Graph();

            ProgressLogger pLogger = new ProgressLogger(logger);
            pLogger.start("Building network...", numEntities);
            osmStream = new FileInputStream(osmFile);
            it = new OsmXmlIterator(osmStream, false);
            for (EntityContainer container : it) {
                if (container.getType() == EntityType.Way) {
                    OsmWay way = (OsmWay) container.getEntity();
                    if (OsmModelUtil.getTagsAsMap(way).containsKey("highway")) {
                        ArrayList<OsmNode> osmWayNodes = new ArrayList(way.getNumberOfNodes());
                        for (int i = 0; i < way.getNumberOfNodes(); i++) {
                            OsmNode node = osmNodes.get(way.getNodeId(i));
                            if (node != null) {
                                    osmWayNodes.add(node);
                            }
                        }
                        for (int i = 0; i < osmWayNodes.size() - 1; i++) {
                            OsmNode from = osmWayNodes.get(i);
                            OsmNode to = osmWayNodes.get(i + 1);

                            boolean endOfWay = (i == 0);
                            Node fromNode = getOrCreateNode(from, graph, endOfWay);

                            endOfWay = (i + 1 == osmWayNodes.size() - 1);
                            Node toNode = getOrCreateNode(to, graph, endOfWay);

                            if (fromNode != null && toNode != null) {
                                Edge edge = new Edge(fromNode, toNode, way.getId(), i);
                                graph.addEdge(edge);
                            }
                        }
                    }
                }
                pLogger.step();
            }
            pLogger.stop();
            logger.info(String.format("Full network: %s node, %s edges.", graph.getNodes().size(), graph.getEdges().size()));

            return graph;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Graph mergeCloseNodes(Graph graph) {
        ProgressLogger pLogger = new ProgressLogger(logger);
        pLogger.start("Snapping close bends...", graph.getEdges().size());
        int cnt = 0;
        for (Edge edge : graph.getEdges()) {
            while (!checkBends(edge)) {
                cnt++;
            }
            pLogger.step();
        }
        pLogger.stop();
        if (cnt > 0) logger.info(String.format("Removed %s bends.", cnt));
        return graph;
    }

    private boolean checkBends(Edge edge) {
        for (int i = 0; i < edge.getBends().size(); i++) {
            Node bend = edge.getBends().get(i);

            /** check previous node */
            Node prev = edge.getFrom();
            if (i > 0) prev = edge.getBends().get(i - 1);
            if (isClose(prev, bend)) {
                edge.getBends().remove(i);
                return false;
            }

            /** check next node */
            Node next = edge.getTo();
            if (i < edge.getBends().size() - 1) next = edge.getBends().get(i + 1);
            if (isClose(bend, next)) {
                edge.getBends().remove(i);
                return false;
            }
        }

        return true;
    }

    private boolean isClose(Node node1, Node node2) {
        double dx = Math.abs(node1.getLongitude() - node2.getLongitude());
        double dy = Math.abs(node1.getLatitude() - node2.getLatitude());

        return (dx < NODE_MERGE_THRESHOLD && dy < NODE_MERGE_THRESHOLD);
    }

    private Node getOrCreateNode(OsmNode osmNode, Graph graph, boolean isEndOfWay) {
//        OsmNode osmNode = osmNodes.get(id);
        if (osmNode != null) {
            Node node = graph.getNode(osmNode.getId());
            if (node == null) {
                node = new Node(osmNode.getId(), osmNode.getLatitude(), osmNode.getLongitude());
                graph.addNode(node);

                Map<String, String> tags = OsmModelUtil.getTagsAsMap(osmNode);
                /**
                 * GraphHopper treads end nodes of ways and some barriers as tower nodes.
                 */
                if (isEndOfWay ||
//                        "stop".equals(tags.get("highway")) ||
                        "gate".equals(tags.get("barrier"))) {
                    endNodes.add(osmNode.getId());
                }
            }
            return node;
        } else {
            return null;
        }
    }
}
