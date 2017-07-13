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

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
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
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author johannes
 */
public class Router {

    private RTree<Long, Point> rTree;

    public Router(InputStream stream) {
        OsmIterator it = new OsmXmlIterator(stream, false);
        rTree = RTree.star().create();
        for (EntityContainer container : it) {
            if (container.getType() == EntityType.Node) {
                OsmNode node = (OsmNode) container.getEntity();
                rTree = rTree.add(node.getId(), Geometries.pointGeographic(node.getLongitude(), node.getLatitude()));
            }
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        String osmFile = "/Users/johannes/Desktop/bockenheim.osm";

        FlagEncoder encoder = new CarFlagEncoder();
        EncodingManager em = new EncodingManager(encoder);

        GraphHopper hopper = new GraphHopperOSM().forDesktop();
        hopper.setDataReaderFile(osmFile);
        hopper.setGraphHopperLocation("/Users/johannes/Desktop/bockenheim-gh/");
        hopper.setEncodingManager(em);
        hopper.setCHEnabled(true);
        hopper.importOrLoad();

        GraphHopperStorage graph = hopper.getGraphHopperStorage();

        LocationIndex index = hopper.getLocationIndex();

        AlgorithmOptions algoOpts = AlgorithmOptions.start().algorithm(Parameters.Algorithms.ASTAR_BI).
                traversalMode(TraversalMode.NODE_BASED).
                weighting(new FastestWeighting(encoder)).
                build();

        QueryResult toQR = index.findClosest(50.1264737, 8.65256, EdgeFilter.ALL_EDGES);
        QueryResult fromQR = index.findClosest(50.1209992, 8.6439189, EdgeFilter.ALL_EDGES);

        QueryGraph queryGraph = new QueryGraph(graph.getGraph(CHGraphImpl.class));
        queryGraph.lookup(fromQR, toQR);

        HintsMap hintsMap = new HintsMap();
        hintsMap.setVehicle("car");
        hintsMap.setWeighting("fastest");

        RoutingAlgorithm algorithm = hopper.getAlgorithmFactory(hintsMap).createAlgo(queryGraph, algoOpts);

        Path path = algorithm.calcPath(fromQR.getClosestNode(), toQR.getClosestNode());

        Router router = new Router(new FileInputStream(osmFile));

        PointList plist = path.calcPoints();
        for (GHPoint point : plist) {
            long id = router.getNode(point.getLon(), point.getLat());
            System.out.println(String.valueOf(id));
        }
    }

    public long getNode(double longitude, double latitude) {
        return rTree.nearest(Geometries.pointGeographic(longitude, latitude), 1, 1).
                first().
                toBlocking().
                single().
                value();
    }
}
