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
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Segment;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

/**
 * @author johannes
 */
public class FacilityDistanceProvider implements ValueProvider<Double, Segment> {

    private final ActivityFacilities facilities;

    public FacilityDistanceProvider(ActivityFacilities facilities) {
        this.facilities = facilities;
    }

    @Override
    public Double get(Segment attributable) {
        Segment prev = attributable.previous();
        Segment next = attributable.next();

        double d = distance(prev, next);
        return d;
    }

    private double distance(Segment from, Segment to) {
        ActivityFacility fac1 = getFacility(from);
        ActivityFacility fac2 = getFacility(to);

        Coord c1 = fac1.getCoord();
        Coord c2 = fac2.getCoord();

        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private ActivityFacility getFacility(Segment act) {
        String facilityId = act.getAttribute(Attributes.KEY.PLACE);
        Id<ActivityFacility> facilityObjId = Id.create(facilityId, ActivityFacility.class);
        ActivityFacility fac = facilities.getFacilities().get(facilityObjId);
        return  fac;
    }
}
