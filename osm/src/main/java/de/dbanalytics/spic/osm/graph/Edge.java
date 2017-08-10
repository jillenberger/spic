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

import java.util.ArrayList;
import java.util.List;

/**
 * @author jillenberger
 */
public class Edge {

    private final Node from;

    private final Node to;

    private final List<Node> bends;

    private final long osmWayId;

    private final int osmWayIndex;

    public Edge(Node from, Node to, long osmWayId, int osmWayIndex) {
        this.from = from;
        this.to = to;
        this.osmWayId = osmWayId;
        this.osmWayIndex = osmWayIndex;

        this.bends = new ArrayList<>();
    }

    public List<Node> getBends() {
        return bends;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public long getOsmWayId() {
        return osmWayId;
    }

    public int getOsmWayIndex() {
        return osmWayIndex;
    }
}
