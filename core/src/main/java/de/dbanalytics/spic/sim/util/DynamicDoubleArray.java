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

import java.util.Arrays;

/**
 * @author johannes
 */
public class DynamicDoubleArray {

    public final double naValue;

    private double[] array;

    public DynamicDoubleArray() {
        naValue = Double.NaN;
        array = new double[12];
        Arrays.fill(array, naValue);
    }


    public DynamicDoubleArray(int size, double naValue) {
        this.naValue = naValue;
        array = new double[size];
        Arrays.fill(array, naValue);
    }

    public void set(int index, double value) {
        if(checkBounds(index)) {
            array[index] = value;
        } else {
            int newLength = index + 1;
            double[] copy = new double[newLength];
            Arrays.fill(copy, naValue);
            System.arraycopy(array, 0, copy, 0, Math.min(array.length, newLength));
            array = copy;
            array[index] = value;
        }
    }

    public double get(int index) {
        if(checkBounds(index)) return array[index];
        else return naValue;
    }

    private boolean checkBounds(int index) {
        return array.length > index;
    }

    public int size() {
        return array.length;
    }
}
