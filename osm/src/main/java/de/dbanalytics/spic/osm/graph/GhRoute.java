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
import gnu.trove.map.TIntObjectMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 */
public class GhRoute implements Route, RouteLeg {

    private static final Logger logger = Logger.getLogger(GhRoute.class);

    private final Path ghPath;

    private final TIntObjectMap<List<Node>> ghEdge2Nodes;

    private List<RouteLeg> routeLegs = new ArrayList<>(1);

    private long[] nodes;

    private final String mode;

    public GhRoute(Path ghPath, TIntObjectMap<List<Node>> ghEdge2Nodes, String mode) {
        this.ghPath = ghPath;
        this.ghEdge2Nodes = ghEdge2Nodes;
        routeLegs.add(this);
        this.mode = mode;
    }

    public double getDistance() {
        return ghPath.getDistance();
    }

    public double distance() {
        return ghPath.getDistance();
    }

    @Override
    public String mode() {
        return mode;
    }

    @Override
    public String getAttribute(String key) {
        return null;
    }

    public double getTraveltime() {
        return ghPath.getTime();
    }

    public double traveltime() {
        return ghPath.getTime() / 1000.0;
    }

    public List<RouteLeg> routeLegs() {
        return routeLegs;
    }

    public long[] nodes() {
        if (nodes == null) {
            List<Node> path = getPath();
            nodes = new long[path.size()];
            for (int i = 0; i < path.size(); i++) {
                nodes[i] = path.get(i).getId();
            }
        }
        return nodes;
    }

    public List<Node> getPath() {
        List<EdgeIteratorState> ghEdges = ghPath.calcEdges();

        /** map all GH-edges to Node-based edges */
        List<List<Node>> osmEdges = new ArrayList<>(ghEdges.size());
        int numNodes = 0;
        for (EdgeIteratorState ghEdge : ghEdges) {
            List<Node> osmEdge = ghEdge2Nodes.get(ghEdge.getEdge());
            /** virtual GH-edges are not mapped */
            if (osmEdge != null) {
                osmEdges.add(osmEdge);
                numNodes += osmEdge.size();
            }
        }

        List<Node> nodes = new ArrayList<>(numNodes);
        if (osmEdges.size() == 0) {
            return nodes;
        } else {
            if (osmEdges.size() == 1) {
                List<Node> edgeNodes = osmEdges.get(0);
                nodes.addAll(edgeNodes);
            } else {
                /** detect the correct order of the first edge*/
                List<Node> firstEdge = osmEdges.get(0);
                List<Node> secondEdge = osmEdges.get(1);

                if (firstEdge.get(firstEdge.size() - 1) == secondEdge.get(0)) {
                    /** both edges correct order */
                    nodes.addAll(firstEdge);
                } else if (firstEdge.get(0) == secondEdge.get(0)) {
                    /** fist edge reversed  - second correct */
                    for (int k = firstEdge.size() - 1; k >= 0; k--) {
                        nodes.add(firstEdge.get(k));
                    }
                } else if (firstEdge.get(0) == secondEdge.get(secondEdge.size() - 1)) {
                    /** both edges reversed */
                    for (int k = firstEdge.size() - 1; k >= 0; k--) {
                        nodes.add(firstEdge.get(k));
                    }
                } else if (firstEdge.get(firstEdge.size() - 1) == secondEdge.get(secondEdge.size() - 1)) {
                    /** second edge reversed */
                    nodes.addAll(firstEdge);
                } else {
                    StringBuilder firstIds = new StringBuilder();
                    firstEdge.stream().forEach(node -> {
                        firstIds.append(node.getId());
                        firstIds.append(" ");
                    });
                    StringBuilder secondIds = new StringBuilder();
                    firstEdge.stream().forEach(node -> {
                        secondIds.append(node.getId());
                        secondIds.append(" ");
                    });
                    logger.warn(String.format("Non consecutive edges: %s - %s",
                            firstIds.toString(),
                            secondIds.toString()));
                    return null;
                }

                /** proceed with the remaining edges */
                for (int i = 1; i < osmEdges.size(); i++) {
                    List<Node> edgeNodes = osmEdges.get(i);

                    if (edgeNodes.size() == 1) {
                        /** Appears to happen when routes and at a barrier.
                         * Add the single node if it is not already in the path.
                         **/
                        if (nodes.get(nodes.size() - 1) != edgeNodes.get(0)) nodes.add(edgeNodes.get(0));
                    } else {
                        Node last = nodes.get(nodes.size() - 1);
                        if (last == edgeNodes.get(0)) {
                            /** correct order */
                            for (int k = 1; k < edgeNodes.size(); k++) {
                                nodes.add(edgeNodes.get(k));
                            }
                        } else if (last == edgeNodes.get(edgeNodes.size() - 1)) {
                            /** reversed order */
                            for (int k = edgeNodes.size() - 2; k >= 0; k--) {
                                nodes.add(edgeNodes.get(k));
                            }
                        } else {
                            StringBuilder builder = new StringBuilder();
                            edgeNodes.stream().forEach(node -> {
                                builder.append(node.getId());
                                builder.append(" ");
                            });
                            logger.warn(String.format("Non consecutive edges: %s - %s",
                                    last.getId(),
                                    builder.toString()));
                            return null;
                        }
                    }
                }
            }
        }

        return nodes;
    }

    public TLongArrayList getPathAsOsmIds() {
        List<Node> path = getPath();
        TLongArrayList ids = new TLongArrayList(path.size());
        for (Node node : path) ids.add(node.getId());
        return ids;
    }
}
