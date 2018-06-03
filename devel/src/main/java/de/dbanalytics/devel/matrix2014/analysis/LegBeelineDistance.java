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

package de.dbanalytics.devel.matrix2014.analysis;

import de.dbanalytics.spic.analysis.ValueProvider;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

/**
 * @author johannes
 */
public class LegBeelineDistance implements ValueProvider<Double, Segment> {

    private final ActivityFacilities facilities;

    public LegBeelineDistance(ActivityFacilities facilities) {
        this.facilities = facilities;
    }

    @Override
    public Double get(Segment leg) {
        Segment prev = leg.previous();
        Segment next = leg.next();

        String prevFacId = prev.getAttribute(CommonKeys.PLACE);
        String nextFacId = next.getAttribute(CommonKeys.PLACE);

        ActivityFacility prevFac = facilities.getFacilities().get(Id.create(prevFacId, ActivityFacility.class));
        ActivityFacility nextFac = facilities.getFacilities().get(Id.create(nextFacId, ActivityFacility.class));

        if(prevFac != null && nextFac != null) {
            double dx = prevFac.getCoord().getX() - nextFac.getCoord().getX();
            double dy = prevFac.getCoord().getY() - nextFac.getCoord().getY();

            return Math.sqrt(dx * dx + dy *dy);
        } else return null;
    }
}
