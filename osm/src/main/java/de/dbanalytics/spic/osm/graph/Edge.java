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
        childEdges.add(this);
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

    public void addChildEdges(List<Edge> edges) {
        childEdges.addAll(edges);
    }
}
