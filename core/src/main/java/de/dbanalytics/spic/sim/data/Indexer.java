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

package de.dbanalytics.spic.sim.data;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jillenberger
 */
public class Indexer {

    private static Map<String, Container> keys = new HashMap<>();

    private static TIntObjectMap<Container> indices = new TIntObjectHashMap<>(
            Constants.DEFAULT_CAPACITY,
            Constants.DEFAULT_LOAD_FACTOR,
            -1);

    private static int maxIndex = 0;

    public static int register(String qualifiedName, Converter converter) {
        Container c = keys.get(qualifiedName);
        if (c == null) {
            c = new Container();
            c.index = maxIndex;
            c.key = qualifiedName;
            c.converter = converter;

            keys.put(qualifiedName, c);
            indices.put(c.index, c);

            maxIndex++;
        }

        return c.index;
    }

    public static String toString(String key, Object value) {
        Container c = keys.get(key);
        if (c == null) return null;
        else return c.converter.toString(value);
    }

    public static Object toObject(int index, String value) {
        Container c = indices.get(index);
        if (c == null) return null;
        else return c.converter.toObject(value);
    }

    public static int getIndex(String key) {
        Container c = keys.get(key);
        if (c == null) return -1;
        else return c.index;
    }

    public static String getKey(int index) {
        Container c = indices.get(index);
        if (c == null) return null;
        else return c.key;
    }

    private static class Container {

        private int index;

        private String key;

        private Converter converter;
    }
}
