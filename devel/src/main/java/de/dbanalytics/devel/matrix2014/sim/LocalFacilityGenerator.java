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
import de.dbanalytics.devel.matrix2014.gis.FacilityData;
import de.dbanalytics.spic.sim.ValueGenerator;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedSegment;
import de.dbanalytics.spic.sim.data.Converters;
import org.matsim.api.core.v01.Coord;
import org.matsim.facilities.ActivityFacility;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jillenberger
 */
public class LocalFacilityGenerator implements ValueGenerator {

    private final FacilityData facilityData;

    private final List<String> blacklist;

    private  Object facilityDataKey;

    private final Random random;

    public LocalFacilityGenerator(FacilityData data, Random random) {
        this.facilityData = data;
        this.blacklist = new ArrayList<>();
        this.random = random;
    }

    public void addToBlacklist(String type) {
        blacklist.add(type);
    }

    @Override
    public Object newValue(CachedElement element) {
        CachedSegment act = (CachedSegment) element;

        /*
        Won't work if activity types change.
         */
        String type = act.getAttribute(CommonKeys.ACTIVITY_TYPE);
        boolean ignore = false;
            if (type != null) {
                if (blacklist.contains(type)) ignore = true;
            }
        if (!ignore) {
            CachedSegment leg = (CachedSegment) act.previous();
            CachedSegment prev = null;
            if(leg != null) prev = (CachedSegment) leg.previous();

            if(prev != null) {
                if(facilityDataKey == null) facilityDataKey = Converters.getObjectKey(CommonKeys.ACTIVITY_FACILITY);
                ActivityFacility prefFac = (ActivityFacility) prev.getData(facilityDataKey);
                for(int i = 0; i < 100; i++) {
                    ActivityFacility newFac = facilityData.randomFacility(type);
                    double d = distance(prefFac, newFac);
                    d = d/1000.0;
                    double p = Math.pow(d, -0.5);
                    if(p >= random.nextDouble()) {
                        return newFac;
                    }
                }
                /*
                if no suitable facility is found, draw a random one
                 */
                return facilityData.randomFacility(type);
            } else {
                return facilityData.randomFacility(type);
            }
        } else {
            return null;
        }
    }

    private double distance(ActivityFacility f1, ActivityFacility f2) {
        Coord c1 = f1.getCoord();
        Coord c2 = f2.getCoord();
        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();

        return Math.sqrt(dx * dx + dy * dy);
    }
}
