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
package de.dbanalytics.spic.sim.util;

import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;

import java.util.Arrays;

/**
 * @author jillenberger
 */
public class DynamicArrayBuilder {

    public static DynamicDoubleArray build(TDoubleDoubleMap hist, Discretizer discretizer) {
        double[] keys = hist.keys();
        Arrays.sort(keys);

        DynamicDoubleArray arr = new DynamicDoubleArray(keys.length, 0);
        for(double key : keys) {
            int idx = discretizer.index(key);
            double val = hist.get(key);
            arr.set(idx, val);
        }

        return arr;
    }
}
