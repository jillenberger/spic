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

package de.dbanalytics.devel.matrix2014.data;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.devel.matrix2014.gis.FacilityData;
import de.dbanalytics.spic.processing.SegmentTask;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.facilities.ActivityFacility;

/**
 * @author johannes
 */
public class RouteLeg implements SegmentTask {

    private static final String SEPARATOR = " ";

    private LeastCostPathCalculator router;

    private FacilityData facilityData;

    private Network network;

    public RouteLeg(LeastCostPathCalculator router, FacilityData facilityData, Network network) {
        this.router = router;
        this.facilityData = facilityData;
        this.network = network;
    }

    @Override
    public void apply(Segment segment) {
        Segment prev = segment.previous();
        Segment next = segment.next();

        if(prev != null && next != null) {
            String prevId = prev.getAttribute(CommonKeys.PLACE);
            String nextId = next.getAttribute(CommonKeys.PLACE);

            if(prevId != null && nextId != null) {
                ActivityFacility startFac = facilityData.getAll().getFacilities().get(Id.create(prevId, ActivityFacility.class));
                ActivityFacility endFac = facilityData.getAll().getFacilities().get(Id.create(nextId, ActivityFacility.class));

                Id<Link> startLinkId = startFac.getLinkId();
                Id<Link> endLinkId = endFac.getLinkId();

                Node startNode = network.getLinks().get(startLinkId).getToNode();
                Node endNode = network.getLinks().get(endLinkId).getFromNode();

                LeastCostPathCalculator.Path path = router.calcLeastCostPath(startNode, endNode, 0, null, null);

                StringBuilder builder = new StringBuilder();
                builder.append(startLinkId.toString());
                for(Link link : path.links) {
                    builder.append(SEPARATOR);
                    builder.append(link.getId().toString());
                }
                builder.append(SEPARATOR);
                builder.append(endLinkId.toString());

                segment.setAttribute(CommonKeys.LEG_ROUTE, builder.toString());
            }
        }
    }
}
