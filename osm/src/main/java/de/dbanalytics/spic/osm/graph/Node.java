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
public class Node {

    private final long id;

    private List<Edge> edges;

    private boolean isEndNode = false;

    public Node(Long id) {
        this.id = id;
        this.edges = new ArrayList<>();
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
