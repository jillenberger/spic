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

package de.dbanalytics.spic.matrix;


import java.util.Collection;
import java.util.Set;

/**
 * @author johannes
 * 
 */
public class NumericMatrix implements Matrix<String, Double> {

	private final Matrix<String, Double> matrix;

	public NumericMatrix() {
		matrix = new HashMatrix<>();
	}

	public Double add(String key1, String key2, double value) {
		Double val = matrix.get(key1, key2);
		if(val == null) {
			return matrix.set(key1, key2, value);
		} else {
			return matrix.set(key1, key2, val + value);
		}
	}

	public void multiply(String i, String j, double factor) {
		Double val = matrix.get(i, j);
		if(val != null) {
			matrix.set(i, j, val * factor);
		}
	}

    @Override
    public Double set(String row, String column, Double value) {
        return matrix.set(row, column, value);
    }

    @Override
    public Double get(String row, String column) {
        return matrix.get(row, column);
    }

    @Override
    public Set<String> keys() {
        return matrix.keys();
    }

    @Override
    public Collection<Double> values() {
        return matrix.values();
    }
}
