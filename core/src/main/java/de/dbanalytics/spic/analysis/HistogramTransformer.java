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
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.matsim.contrib.common.stats.Histogram;

/**
 * @author jillenberger
 */
public class HistogramTransformer {

    public static TDoubleDoubleMap transform(TDoubleArrayList borders, TDoubleDoubleMap source) {
        HistogramFunction function = new HistogramFunction(source);

        TDoubleDoubleMap newHist = new TDoubleDoubleHashMap();
        borders.forEach(value -> {
            newHist.put(value, function.value(value));
            return true;
        });

        double norm = Histogram.sum((TDoubleDoubleHashMap) newHist) / Histogram.sum((TDoubleDoubleHashMap) source);
        Histogram.normalize((TDoubleDoubleHashMap) newHist, norm);

        return newHist;
    }
}
