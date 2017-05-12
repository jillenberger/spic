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

package de.dbanalytics.spic.osm.graph;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author johannes
 */
public class GraphBuilder {

    public void build(String filename) throws FileNotFoundException {
        InputStream osmStream = new FileInputStream(filename);

        OsmIterator it = new OsmXmlIterator(osmStream, false);
        TLongObjectMap<Node> nodes = new TLongObjectHashMap<>();
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                Node node = new Node(container.getEntity().getId());
                nodes.put(node.getId(), node);
            }
        }

        it = new OsmXmlIterator(osmStream, false);

        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();

                for (int i = 0; i < way.getNumberOfNodes() - 1; i++) {
                    long fromId = way.getNodeId(i);
                    long toId = way.getNodeId(i + 1);

                    Node from = nodes.get(fromId);
                    Node to = nodes.get(toId);
                    if (from != null && to != null) {
                        Edge edge = new Edge(from, to);
                        from.addEdge(edge);
                        to.addEdge(edge);
                    }
                }
            }
        }

        Queue<Node> pillars = new LinkedList<>();
        TLongObjectIterator<Node> nodeIt = nodes.iterator();
        while (nodeIt.hasNext()) {
            nodeIt.advance();
            Node node = nodeIt.value();
            if (node.getEdges().size() == 2) {
                pillars.add(node);
            }
        }

        while (!pillars.isEmpty()) {
            Node pillar = pillars.poll();
            Edge edge1 = pillar.getEdges().get(0);
            Edge edge2 = pillar.getEdges().get(1);

            Node tower1 = edge1.getFrom();
            if (tower1 == pillar) tower1 = edge1.getTo();

            Node tower2 = edge2.getFrom();
            if (tower2 == pillar) tower2 = edge2.getTo();

            tower1.getEdges().remove(edge1);
            tower2.getEdges().remove(edge2);

            Edge towerEdge = new Edge(tower1, tower2);
            tower1.addEdge(towerEdge);
            tower2.addEdge(towerEdge);

            towerEdge.addChildEdges(edge1.getChildEdges());
            towerEdge.addChildEdges(edge2.getChildEdges());

            if (tower1.getEdges().size() == 2) pillars.add(tower1);
            if (tower2.getEdges().size() == 2) pillars.add(tower2);
        }
    }
}
