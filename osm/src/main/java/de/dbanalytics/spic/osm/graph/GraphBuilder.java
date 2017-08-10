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
import java.util.LinkedList;
import java.util.List;

/**
 * @author jillenberger
 */
public class GraphBuilder {

    private static final Logger logger = Logger.getLogger(GraphBuilder.class);

    private TLongArrayList endNodes;

    public Graph build(String osmFile) {
        endNodes = new TLongArrayList();
        Graph graph = buildFullNetwork(osmFile);
        graph = buildeTowerNetwork(graph);
        return graph;
    }

    private Graph buildeTowerNetwork(Graph graph) {
        /** get pillar (bend) nodes */
        endNodes.sort();
        List<Node> pillars = new LinkedList<>();
        for (Node node : graph.getNodes()) {
            if (node.getEdges().size() == 2 && endNodes.binarySearch(node.getId()) < 0) {
                pillars.add(node);
            }
        }

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

        }

        return graph;
    }

    private Graph buildFullNetwork(String osmFile) {
        try {
            logger.info("Loading nodes...");
            InputStream osmStream = new FileInputStream(osmFile);

            OsmIterator it = new OsmXmlIterator(osmStream, false);
            TLongObjectMap<OsmNode> osmNodes = new TLongObjectHashMap<>();
            for (EntityContainer container : it) {
                if (container.getType() == EntityType.Node) {
                    OsmNode osmNode = (OsmNode) container.getEntity();
                    osmNodes.put(osmNode.getId(), (OsmNode) container.getEntity());
                }
            }
            logger.info(String.format("Loaded %s OSM nodes.", osmNodes.size()));

            Graph graph = new Graph();

            logger.info("Building full network...");
            osmStream = new FileInputStream(osmFile);
            it = new OsmXmlIterator(osmStream, false);
            for (EntityContainer container : it) {
                if (container.getType() == EntityType.Way) {
                    OsmWay way = (OsmWay) container.getEntity();
                    if (OsmModelUtil.getTagsAsMap(way).containsKey("highway")) {
                        for (int i = 0; i < way.getNumberOfNodes() - 1; i++) {
                            long fromId = way.getNodeId(i);
                            long toId = way.getNodeId(i + 1);

                            boolean endOfWay = (i == 0);
                            Node fromNode = getOrCreateNode(fromId, osmNodes, graph, endOfWay);

                            endOfWay = (i + 1 == way.getNumberOfNodes() - 1);
                            Node toNode = getOrCreateNode(toId, osmNodes, graph, endOfWay);

                            if (fromNode != null && toNode != null) {
                                Edge edge = new Edge(fromNode, toNode, way.getId(), i);
                                graph.addEdge(edge);
                            }
                        }
                    }
                }
            }

            logger.info(String.format("Full network: %s node, %s edges.", graph.getNodes().size(), graph.getEdges().size()));

            return graph;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Node getOrCreateNode(long id, TLongObjectMap<OsmNode> osmNodes, Graph graph, boolean isEndOfWay) {
        OsmNode osmNode = osmNodes.get(id);
        if (osmNode != null) {
            Node node = graph.getNode(id);
            if (node == null) {
                node = new Node(osmNode.getId(), osmNode.getLatitude(), osmNode.getLongitude());
                graph.addNode(node);

                if (isEndOfWay) endNodes.add(id);
            }
            return node;
        } else {
            return null;
        }
    }
}
