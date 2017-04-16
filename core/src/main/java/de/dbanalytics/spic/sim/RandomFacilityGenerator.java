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
package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.gis.FacilityData;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedSegment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jillenberger
 */
public class RandomFacilityGenerator implements ValueGenerator {

    private static final Object IGNORE_KEY = new Object();

    private final FacilityData facilityData;

    private final List<String> blacklist;

    public RandomFacilityGenerator(FacilityData data) {
        this.facilityData = data;
        this.blacklist = new ArrayList<>();
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
//        Boolean ignore = (Boolean) act.getData(IGNORE_KEY);
//        if (ignore == null) {
//            ignore = false;
//
            if (type != null) {
                if (blacklist.contains(type)) ignore = true;
            }
//
//            act.setData(IGNORE_KEY, ignore);
//        }

        if (!ignore) {
            return facilityData.randomFacility(type);
        } else {
            return null;
        }
    }
}
