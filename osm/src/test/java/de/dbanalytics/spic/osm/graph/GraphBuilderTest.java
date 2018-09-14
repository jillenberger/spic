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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author johannes
 */
public class GraphBuilderTest extends TestCase {

    public void testImport() {
        GraphBuilder builder = new GraphBuilder();
        Graph graph = builder.build("src/test/resources/bockenheim.osm");

        /** Test edge 1 */
        Node node = graph.getNode(16421603);

        Edge edge = null;
        for (Edge e : node.getEdges()) {
            if (e.getFrom().getId() == 423205743) edge = e;
        }

        Assert.assertNotNull(edge);
        Assert.assertEquals(graph.getNode(16421603), edge.getTo());
        Assert.assertEquals(graph.getNode(423205743), edge.getFrom());
        Assert.assertEquals(2472449851L, edge.getBends().get(0).getId());
        Assert.assertEquals(24226224, edge.getOsmWayId());
        Assert.assertEquals(6, edge.getOsmWayIndex());

        Assert.assertEquals(0, edge.getBends().get(0).getEdges().size());

        /** Test edge 2 */
        node = graph.getNode(16421101);

        edge = null;
        for (Edge e : node.getEdges()) {
            if (e.getTo().getId() == 633415473) edge = e;
        }

        Assert.assertNotNull(edge);
        Assert.assertEquals(graph.getNode(633415473), edge.getTo());
        Assert.assertEquals(graph.getNode(16421101), edge.getFrom());
        Assert.assertEquals(16421343, edge.getBends().get(0).getId());
        Assert.assertEquals(16421222, edge.getBends().get(1).getId());
        Assert.assertEquals(4067238295L, edge.getBends().get(2).getId());
        Assert.assertEquals(4759172, edge.getOsmWayId());
        Assert.assertEquals(6, edge.getOsmWayIndex());

        for (Node bend : edge.getBends()) Assert.assertEquals(0, bend.getEdges().size());

    }
}
