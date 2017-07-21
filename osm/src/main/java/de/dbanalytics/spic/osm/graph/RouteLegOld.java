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
import com.graphhopper.routing.AlgorithmOptions;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.RoutingAlgorithm;
import com.graphhopper.routing.ch.PrepareContractionHierarchies;
import com.graphhopper.routing.util.DefaultEdgeFilter;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.FastestWeighting;
import com.graphhopper.storage.CHGraphImpl;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.index.LocationIndex;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Parameters;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.FacilityData;
import de.dbanalytics.spic.processing.SegmentTask;
import gnu.trove.list.TIntList;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacility;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * @author johannes
 */
public class RouteLegOld implements SegmentTask {

    private static final String SEPARATOR = " ";

    private final FacilityData facilityData;

    private final LocationIndex locationIndex;

    private final MathTransform transform;

    private final PrepareContractionHierarchies chAlgoFactory;

    private final AlgorithmOptions options;

    private final GraphHopperStorage graph;

    private final EdgeFilter edgeFilter;

    private final Graph chGraph;

    public RouteLegOld(GraphHopper graphHopper, FlagEncoder encoder, FacilityData facilityData, MathTransform transform) {
        this.facilityData = facilityData;
        this.locationIndex = graphHopper.getLocationIndex();
        this.transform = transform;
        this.graph = graphHopper.getGraphHopperStorage();

        chAlgoFactory = new PrepareContractionHierarchies(graph.getDirectory(),
                graph,
                graph.getGraph(CHGraphImpl.class),
                new FastestWeighting(encoder),
                TraversalMode.NODE_BASED);

        options = AlgorithmOptions.start().algorithm(Parameters.Algorithms.ASTAR_BI).
                traversalMode(TraversalMode.NODE_BASED).
                weighting(new FastestWeighting(encoder)).
                build();

        edgeFilter = new DefaultEdgeFilter(encoder);

        chGraph = graph.getGraph(CHGraphImpl.class);

    }

    @Override
    public void apply(Segment segment) {
        Segment prev = segment.previous();
        Segment next = segment.next();

        if(prev != null && next != null) {
            String prevId = prev.getAttribute(CommonKeys.ACTIVITY_FACILITY);
            String nextId = next.getAttribute(CommonKeys.ACTIVITY_FACILITY);

            if(prevId != null && nextId != null) {
                ActivityFacility startFac = facilityData.getAll().getFacilities().get(Id.create(prevId, ActivityFacility.class));
                ActivityFacility endFac = facilityData.getAll().getFacilities().get(Id.create(nextId, ActivityFacility.class));

                double[] fromCoord = calcWGS84Coords(startFac);
                double[] toCoord = calcWGS84Coords(endFac);

                QueryResult fromQuery = locationIndex.findClosest(fromCoord[1], fromCoord[0], edgeFilter);
                QueryResult toQuery = locationIndex.findClosest(toCoord[1], toCoord[0], edgeFilter);

                if(!fromQuery.isValid() || !toQuery.isValid()) {
                    return;
                }

                RoutingAlgorithm algorithm = chAlgoFactory.createAlgo(chGraph, options);
                Path path = algorithm.calcPath(fromQuery.getClosestNode(), toQuery.getClosestNode());
                TIntList nodes = path.calcNodes();

                if(nodes.size() > 0) {
                    StringBuilder builder = new StringBuilder();
                    builder.append(String.valueOf(nodes.get(0)));
                    for (int i = 1; i < nodes.size(); i++) {
                        builder.append(SEPARATOR);
                        builder.append(String.valueOf(nodes.get(i)));
                    }
                    segment.setAttribute(CommonKeys.LEG_ROUTE, builder.toString());
                }
            }
        }
    }

    private double[] calcWGS84Coords(ActivityFacility facility) {
        double[] points = new double[] { facility.getCoord().getX(), facility.getCoord().getY() };
        try {
            transform.transform(points, 0, points, 0, 1);
        } catch (TransformException e) {
            e.printStackTrace();
        }
        return points;
    }
}
