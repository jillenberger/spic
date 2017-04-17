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

import gnu.trove.function.TDoubleFunction;
import gnu.trove.map.TObjectDoubleMap;

/**
 * @author johannes
 */
public class Histogram {

    public static TObjectDoubleMap<?> normalize(TObjectDoubleMap<?> histogram) {
        double sum = 0;
        double[] values = histogram.values();

        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return normalize(histogram, sum);
    }

    public static TObjectDoubleMap<?> normalize(TObjectDoubleMap<?> histogram, double sum) {
        final double norm = 1 / sum;

        TDoubleFunction fct = new TDoubleFunction() {
            public double execute(double value) {
                return value * norm;
            }

        };

        histogram.transformValues(fct);

        return histogram;
    }
}
