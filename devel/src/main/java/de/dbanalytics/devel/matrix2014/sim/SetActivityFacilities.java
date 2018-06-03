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

package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.devel.matrix2014.gis.FacilityData;
import de.dbanalytics.spic.processing.EpisodeTask;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.facilities.ActivityFacility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author johannes
 */
public class SetActivityFacilities implements EpisodeTask {

    private final FacilityData data;

    private final Random random;

    private final double rangeFactor;

    private final double fallbackDistance = 10000;

    public SetActivityFacilities(FacilityData data) {
        this(data, 0.1, new XORShiftRandom());
    }

    public SetActivityFacilities(FacilityData data, double rangeFactor, Random random) {
        this.data = data;
        this.random = random;
        this.rangeFactor = rangeFactor;
    }

    @Override
    public void apply(Episode episode) {
        for(Segment act : episode.getActivities()) {
            if(act.getAttribute(CommonKeys.PLACE) == null) {
                String type = act.getAttribute(CommonKeys.TYPE);
                ActivityFacility facility = null;
                Segment toLeg = act.previous();
                /*
                get random facility in distance range
                 */
                if(toLeg != null) {
                    Segment prevAct = toLeg.previous();
                    Id<ActivityFacility> originId = Id.create(
                            prevAct.getAttribute(CommonKeys.PLACE),
                            ActivityFacility.class);
                    ActivityFacility origin = data.getAll().getFacilities().get(originId);

                    String distance = toLeg.getAttribute(CommonKeys.BEELINE_DISTANCE);
                    double d = fallbackDistance;
                    if(distance != null) d = Double.parseDouble(distance);

                    QuadTree<ActivityFacility> quadTree = data.getQuadTree(type);

                    double range = d * rangeFactor;
                    List<ActivityFacility> facilityList = new ArrayList<>(quadTree.getRing(
                            origin.getCoord().getX(),
                            origin.getCoord().getY(),
                            d - range,
                            d + range));

                    if(facilityList.isEmpty())
                        facility = quadTree.getClosest(origin.getCoord().getX(), origin.getCoord().getY());
                    else
                        facility = facilityList.get(random.nextInt(facilityList.size()));

                }
                /*
                fallback to random facility
                 */
                if(facility == null) {
                    facility = data.randomFacility(type);
                }

                act.setAttribute(CommonKeys.PLACE, facility.getId().toString());
            }
        }
    }
}
