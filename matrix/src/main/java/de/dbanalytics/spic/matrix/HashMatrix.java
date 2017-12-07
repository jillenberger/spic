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

import java.util.*;

/**
 * @author johannes
 */
public class HashMatrix<K, V> implements Matrix<K, V> {

    private Map<K, Map<K, V>> matrix;

    public HashMatrix() {
        matrix = new HashMap<>();
    }

    public V set(K row, K column, V value) {
        Map<K, V> rowCells = matrix.get(row);
        if (rowCells == null) {
            rowCells = new HashMap<>();
            matrix.put(row, rowCells);
        }

        return rowCells.put(column, value);
    }

    public V get(K row, K column) {
        Map<K, V> rowCells = getRow(row);
        if (rowCells == null) {
            return null;
        } else {
            return rowCells.get(column);
        }
    }

    public Map<K, V> getRow(K row) {
        return matrix.get(row);
    }

    public Map<K, Map<K, V>> getRows() {
        return matrix;
    }

    public Set<K> keys() {
        Set<K> keys = new HashSet<>(matrix.keySet());
        for (Map.Entry<K, Map<K, V>> entry : matrix.entrySet()) {
            keys.addAll(entry.getValue().keySet());
        }

        return keys;
    }

    public Collection<V> values() {
        List<V> values = new ArrayList<>();
        for (Map.Entry<K, Map<K, V>> entry : matrix.entrySet()) {
            values.addAll(entry.getValue().values());
        }
        return values;
    }
}
