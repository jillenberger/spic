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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * @author johannes
 */
public class TowerNodeNetwork {

    private static final Logger logger = Logger.getLogger(TowerNodeNetwork.class);
    private final Set<Edge> towerEdges;

    public TowerNodeNetwork(String filename) throws FileNotFoundException {
        logger.info("Loading nodes...");
        InputStream osmStream = new FileInputStream(filename);
        OsmIterator it = new OsmXmlIterator(osmStream, false);
//        TLongObjectMap<OsmNode> allNodes = new TLongObjectHashMap<>();
        TLongObjectMap<OsmNode> osmNodes = new TLongObjectHashMap<>();
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                OsmNode osmNode = (OsmNode) container.getEntity();
                osmNodes.put(osmNode.getId(), (OsmNode) container.getEntity());
            }
        }
        logger.info(String.format("Loaded %s nodes.", osmNodes.size()));

        TLongObjectMap<Node> nodes = new TLongObjectHashMap<>();

        logger.info("Loading edges...");
        osmStream = new FileInputStream(filename);
        it = new OsmXmlIterator(osmStream, false);
        int pillarEdges = 0;
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();
                if (OsmModelUtil.getTagsAsMap(way).containsKey("highway")) {


                    for (int i = 0; i < way.getNumberOfNodes() - 1; i++) {
                        long fromId = way.getNodeId(i);
                        long toId = way.getNodeId(i + 1);

                        Node from = nodes.get(fromId);
                        OsmNode osmFrom = osmNodes.get(fromId);
                        if (from == null && osmFrom != null) {
                            from = new Node(fromId, osmFrom.getLatitude(), osmFrom.getLongitude());
                            nodes.put(fromId, from);
                        }

                        Node to = nodes.get(toId);
                        OsmNode osmTo = osmNodes.get(toId);
                        if (to == null && osmTo != null) {
                            to = new Node(toId, osmTo.getLatitude(), osmTo.getLongitude());
                            nodes.put(toId, to);
                        }

                        if (from != null && to != null) {
                            Edge edge = new Edge(from, to);
                            edge.addChildEdge(edge); // add self for base edges
                            from.addEdge(edge);
                            to.addEdge(edge);
                            pillarEdges++;
                        }
                    }

                    Node start = nodes.get(way.getNodeId(0));
                    if (start != null) start.setEndNode();
                    Node end = nodes.get(way.getNodeId(way.getNumberOfNodes() - 1));
                    if (end != null) end.setEndNode();
                }
            }
        }
        logger.info(String.format("Loaded %s nodes nodes and %s pillar edges.", nodes.size(), pillarEdges));
        /**
         * Get all pillar nodes, that is nodes with degree=2.
         */
        logger.info("Building tower graph...");
        Queue<Node> pillars = new LinkedList<>();
        List<Node> towers = new ArrayList<>(nodes.size());

        TLongObjectIterator<Node> nodeIt = nodes.iterator();
        while (nodeIt.hasNext()) {
            nodeIt.advance();
            Node node = nodeIt.value();
            if (node.getIsEndNode() || node.getEdges().size() != 2) {
//            if (node.getEdges().size() != 2) {
                towers.add(node);
            } else {
                pillars.add(node);

            }
        }
        logger.info(String.format("Found %s pillars and %s towers.", pillars.size(), towers.size()));

        logger.info("Processing pillars...");
        while (!pillars.isEmpty()) {
            Node pillar = pillars.poll();
            Edge edge1 = pillar.getEdges().get(0);
            Edge edge2 = pillar.getEdges().get(1);

            Node tower1 = edge1.getFrom();
            if (tower1 == pillar) tower1 = edge1.getTo();

            Node tower2 = edge2.getFrom();
            if (tower2 == pillar) tower2 = edge2.getTo();
            /**
             * unlink edges
             */
            tower1.getEdges().remove(edge1);
            tower2.getEdges().remove(edge2);

            Edge towerEdge = new Edge(tower1, tower2);
            tower1.addEdge(towerEdge);
            tower2.addEdge(towerEdge);

            towerEdge.addChildEdges(edge1.getChildEdges());
            towerEdge.addChildEdges(edge2.getChildEdges());
        }

        towerEdges = new HashSet<>(towers.size());
        for (Node tower : towers) {
            towerEdges.addAll(tower.getEdges());
        }
        logger.info(String.format("%s tower-edges.", towerEdges.size()));
    }

    public static void main(String args[]) throws IOException {
        String osmFile = args[0];
        String outFile = args[1];

        TowerNodeNetwork builder = new TowerNodeNetwork(osmFile);
        Set<Edge> edges = builder.getEdges();

        logger.info("Writing mapping...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        writer.write("tower-edge\tpillar-edge");
        writer.newLine();
        for (Edge edge : edges) {
            /** forward */
            String edgeId = edge.getFrom().getId() + ";" + edge.getTo().getId();
            for (Edge child : edge.getChildEdges()) {
                writer.write(edgeId);
                writer.write("\t");
                writer.write(String.valueOf(child.getFrom().getId()));
                writer.write(";");
                writer.write(String.valueOf(child.getTo().getId()));
                writer.newLine();
            }
            /** backward */
            edgeId = edge.getTo().getId() + ";" + edge.getFrom().getId();
            for (Edge child : edge.getChildEdges()) {
                writer.write(edgeId);
                writer.write("\t");
                writer.write(String.valueOf(child.getTo().getId()));
                writer.write(";");
                writer.write(String.valueOf(child.getFrom().getId()));
                writer.newLine();
            }
        }
        writer.close();
        logger.info("Done.");
    }

    public Set<Edge> getEdges() throws FileNotFoundException {
        return towerEdges;
    }

    /**
     * @author johannes
     */
    public static class Edge {

        private final Node from;

        private final Node to;

        private List<Edge> childEdges;

        public Edge(Node from, Node to) {
            this.from = from;
            this.to = to;
            this.childEdges = new ArrayList<>();
        }

        public Node getFrom() {
            return from;
        }

        public Node getTo() {
            return to;
        }

        public List<Edge> getChildEdges() {
            return childEdges;
        }

        public void addChildEdge(Edge edge) {
            childEdges.add(edge);
        }

        public void addChildEdges(List<Edge> edges) {
            childEdges.addAll(edges);
        }
    }

    /**
     * @author johannes
     */
    public static class Node {

        private final long id;

        private List<Edge> edges;

        private boolean isEndNode = false;

        private double lat;

        private double lon;

        public Node(Long id, double lat, double lon) {
            this.id = id;
            this.edges = new ArrayList<>();
            this.lat = lat;
            this.lon = lon;
        }

        public double getLat() {
            return lat;
        }

        public double getLon() {
            return lon;
        }

        public long getId() {
            return id;
        }

        public void addEdge(Edge edge) {
            edges.add(edge);
        }

        public List<Edge> getEdges() {
            return edges;
        }

        public void setEndNode() {
            isEndNode = true;
        }

        public boolean getIsEndNode() {
            return isEndNode;
        }
    }
}
