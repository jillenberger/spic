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

package de.dbanalytics.spic.analysis;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;

/**
 * @author jillenberger
 */
public class HistogramFunction {

    private final TDoubleDoubleMap histogram;

    private final Discretizer discretizer;

    public HistogramFunction(TDoubleDoubleMap histogram) {
        this.histogram = histogram;

        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(0);
        histogram.forEachKey(key -> borders.add(key));
        discretizer = new FixedBordersDiscretizer(borders.toArray());
    }

    public double value(double x) {
        if (discretizer.index(x) == 0) {
            return histogram.get(x);
        } else {
            double x_upper = discretizer.discretize(x);
            double x_lower = x_upper - discretizer.binWidth(x);

            //TODO: Reweighting should be done before
            double y_upper = histogram.get(x_upper) / discretizer.binWidth(x_upper);
            double y_lower = histogram.get(x_lower) / discretizer.binWidth(x_lower);

            double a = (y_upper - y_lower) / (x_upper - x_lower);
            double y = a * (x - x_lower) + y_lower;

            return y;
        }
    }
}
