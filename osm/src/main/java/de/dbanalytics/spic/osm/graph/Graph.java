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

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author jillenberger
 */
public class Graph {

    private final Set<Edge> edges;

    private final TLongObjectMap<Node> nodes;

    public Graph() {
        edges = new HashSet<>(10000);
        nodes = new TLongObjectHashMap<>();
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodes.valueCollection());
    }

    public Node getNode(long id) {
        return nodes.get(id);
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public void removeNode(Node node) {
        if (node.getEdges().isEmpty()) nodes.remove(node.getId());
        else throw new RuntimeException("Cannot remove a node that is still connected to edges.");
    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
        edge.getFrom().getEdges().add(edge);
        edge.getTo().getEdges().add(edge);
    }

    public void removeEdge(Edge edge) {
        edge.getFrom().getEdges().remove(edge);
        edge.getTo().getEdges().remove(edge);
        edges.remove(edge);
    }


}
