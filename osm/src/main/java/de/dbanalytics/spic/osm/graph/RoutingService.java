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

import com.graphhopper.GraphHopper;
import com.graphhopper.reader.osm.GraphHopperOSM;
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.QueryGraph;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.util.*;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Parameters;

import java.io.FileNotFoundException;

/**
 * @author johannes
 */
public class RoutingService {

    private final GraphHopper hopper;

    private final AlgorithmOptions algoOpts;

    private final HintsMap hintsMap;

    private Edge2Node edge2Node;

    public RoutingService(String osmFile, String tmpDir) {
        FlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = new EncodingManager(encoder);

        hopper = new GraphHopperOSM().forDesktop();
        hopper.setDataReaderFile(osmFile);
        hopper.setGraphHopperLocation(tmpDir);
        hopper.setEncodingManager(em);
        hopper.setCHEnabled(true);
        hopper.importOrLoad();

        algoOpts = AlgorithmOptions.start().algorithm(Parameters.Algorithms.ASTAR_BI).
                traversalMode(TraversalMode.NODE_BASED).
                weighting(new FastestWeighting(encoder)).
                build();

        hintsMap = new HintsMap();
        hintsMap.setVehicle("car");
        hintsMap.setWeighting("fastest");

        try {
            edge2Node = new Edge2Node(hopper.getGraphHopperStorage(), osmFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
    }

    public RoutingResult query(double fromLat, double fromLon, double toLat, double toLon) {
        LocationIndex index = hopper.getLocationIndex();
        GraphHopperStorage graph = hopper.getGraphHopperStorage();

        QueryResult fromQR = index.findClosest(fromLat, fromLon, EdgeFilter.ALL_EDGES);
        QueryResult toQR = index.findClosest(toLat, toLon, EdgeFilter.ALL_EDGES);

        QueryGraph queryGraph = new QueryGraph(graph.getGraph(CHGraphImpl.class));
        queryGraph.lookup(fromQR, toQR);

        RoutingAlgorithm algorithm = hopper.getAlgorithmFactory(hintsMap).createAlgo(queryGraph, algoOpts);

        Path path = algorithm.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());

        if (path.isFound()) return new RoutingResult(path, edge2Node);
        else return null;
    }
}
