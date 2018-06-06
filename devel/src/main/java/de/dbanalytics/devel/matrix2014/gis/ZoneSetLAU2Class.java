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

package de.dbanalytics.devel.matrix2014.gis;

import de.dbanalytics.spic.mid2008.MidAttributes;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;

/**
 * @author johannes
 */
public class ZoneSetLAU2Class {

    private static final Discretizer categories;

    static {
        double[] borders = new double[5];
        borders[0] = 5000;
        borders[1] = 20000;
        borders[2] = 50000;
        borders[3] = 100000;
        borders[4] = 500000;

        categories = new FixedBordersDiscretizer(borders);
    }

    public static String inhabitants2Class(double inhabitants) {
        int idx = categories.index(inhabitants); //TODO: synchronize with PersonLau2CategoryHandler
        return String.valueOf(idx);
    }

    public void apply(ZoneCollection zones) {
        for(Zone zone : zones.getZones()) {
            String inhabitantsVal = zone.getAttribute(ZoneData.POPULATION_KEY);
            if(inhabitantsVal != null) {
                double inhabitants = Double.parseDouble(inhabitantsVal);
                String category = inhabitants2Class(inhabitants);
                zone.setAttribute(MidAttributes.KEY.LAU2_CAT, category);
            }
        }
    }
}
