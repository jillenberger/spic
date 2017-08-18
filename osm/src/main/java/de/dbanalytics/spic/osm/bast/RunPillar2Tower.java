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

package de.dbanalytics.spic.osm.bast;

import de.dbanalytics.spic.osm.graph.Edge;
import de.dbanalytics.spic.osm.graph.Graph;
import de.dbanalytics.spic.osm.graph.GraphBuilder;
import de.dbanalytics.spic.osm.graph.Node;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author johannes
 */
public class RunPillar2Tower {

    public static void main(String args[]) throws IOException {
        String osmFile = "";
        String bastFile = "";

        GraphBuilder builder = new GraphBuilder();
        Graph graph = builder.build(osmFile);

        TLongObjectMap<Edge> bend2EdgeMapping = new TLongObjectHashMap<>();
        for (Edge edge : graph.getEdges()) {
            for (Node bend : edge.getBends()) {
                bend2EdgeMapping.put(bend.getId(), edge);
            }
        }

        BufferedReader reader = new BufferedReader(new FileReader(bastFile));
        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {

        }
    }
}
