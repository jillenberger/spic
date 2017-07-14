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

import com.graphhopper.routing.Path;
import com.graphhopper.util.EdgeIteratorState;
import gnu.trove.list.array.TLongArrayList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 */
public class RoutingResult {

    private final Path ghPath;

    private final Edge2Node edge2Node;

    public RoutingResult(Path ghPath, Edge2Node edge2Node) {
        this.ghPath = ghPath;
        this.edge2Node = edge2Node;
    }

    public double getDistance() {
        return ghPath.getDistance();
    }

    public double getTraveltime() {
        return ghPath.getTime();
    }

    public TLongArrayList getPath() {
        TLongArrayList allNodes = new TLongArrayList();

        List<EdgeIteratorState> edges = ghPath.calcEdges();
        List<TLongArrayList> nodeEdges = new ArrayList<>(edges.size());

        for (EdgeIteratorState edge : edges) {
            TLongArrayList nodes = edge2Node.getNodes(edge.getEdge());
            if (nodes != null) {
                nodeEdges.add(nodes);
            }
        }

        if (nodeEdges.isEmpty()) {
            return new TLongArrayList();
        } else if (nodeEdges.size() == 1) {
            return nodeEdges.get(0);
        } else {
            /** check if first edge needs to be reversed */
            TLongArrayList first = nodeEdges.get(0);
            TLongArrayList second = nodeEdges.get(1);

            if (first.get(first.size() - 1) == second.get(0)) {
                /** both edges in correct order */
                allNodes.addAll(first);
            } else if (first.get(0) == second.get(0)) {
                /** first edge needs to be reversed */
                for (int k = first.size() - 1; k >= 0; k--) allNodes.add(first.get(k));

            } else if (first.get(first.size() - 1) == second.get(second.size() - 1)) {
                /** second edge needs to be reversed */
                allNodes.addAll(first);

            } else if (first.get(0) == second.get(second.size() - 1)) {
                /** both edges need to be reversed */
                for (int k = first.size() - 1; k >= 0; k--) allNodes.add(first.get(k));

            } else {
                throw new RuntimeException("No consecutive edges: " + first.toString() + " -> " + second.toString());
            }

            /** proceed with remaining edges - do not at first node of edge */
            for (int i = 1; i < nodeEdges.size(); i++) {
                long last = allNodes.get(allNodes.size() - 1);
                TLongArrayList next = nodeEdges.get(i);

                if (last == next.get(0)) {
                    /** correct order */
                    for (int k = 1; k < next.size(); k++) {
                        allNodes.add(next.get(k));
                    }
                } else if (last == next.get(next.size() - 1)) {
                    /** reversed order */
                    for (int k = next.size() - 2; k >= 0; k--) {
                        allNodes.add(next.get(k));
                    }
                } else {
                    throw new RuntimeException("No consecutive edges: " + last + " -> " + next.toString());
                }
            }
        }

        return allNodes;
    }
}
