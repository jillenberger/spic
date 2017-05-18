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

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 */
public class Edge {

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
