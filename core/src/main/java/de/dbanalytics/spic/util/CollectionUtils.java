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
package de.dbanalytics.spic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jillenberger
 */
public class CollectionUtils {

    public static List<double[]> toNativeArray(List<Double> values1, List<Double> values2) {
        if(values1.size() != values2.size()) {
            throw new RuntimeException("Values and weights have to have equal length.");
        }

        double[] nativeValues1 = new double[values1.size()];
        double[] nativeValues2 = new double[values2.size()];

        int idx = 0;
        for(int i = 0; i < values1.size(); i++) {
            if(values1.get(i) != null && values2.get(i) != null) {
                nativeValues1[idx] = values1.get(i);
                nativeValues2[idx] = values2.get(i);
                idx++;
            }
        }

        if(idx < values1.size()) {
            nativeValues1 = Arrays.copyOf(nativeValues1, idx);
            nativeValues2 = Arrays.copyOf(nativeValues2, idx);
        }

        List<double[]> list = new ArrayList<>(2);
        list.add(nativeValues1);
        list.add(nativeValues2);

        return list;
    }

    public static <T> List<T> toList(T[] array) {
        ArrayList<T> list = new ArrayList<>(array.length);
        for (T element : array) list.add(element);
        return list;
    }
}
