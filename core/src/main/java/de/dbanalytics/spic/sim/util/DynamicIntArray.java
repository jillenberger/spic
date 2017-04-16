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
public class DynamicIntArray {

    public final int naValue;

    private int[] array;

    public DynamicIntArray() {
        naValue = -Integer.MAX_VALUE;
        array = new int[12];
        Arrays.fill(array, naValue);
    }


    public DynamicIntArray(int size, int naValue) {
        this.naValue = naValue;
        array = new int[size];
        Arrays.fill(array, naValue);
    }

    public void set(int index, int value) {
        if(checkBounds(index)) {
            array[index] = value;
        } else {
            int newLength = index + 1;
            int[] copy = new int[newLength];
            Arrays.fill(copy, naValue);
            System.arraycopy(array, 0, copy, 0, Math.min(array.length, newLength));
            array = copy;
            array[index] = value;
        }
    }

    public int get(int index) {
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
