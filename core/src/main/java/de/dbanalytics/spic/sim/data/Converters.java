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

import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 */
public class Converters {

    private static Map<String, Container> plainKeys = new HashMap<>();

    private static Map<Object, Container> objKeys = new HashMap<>();

    public static Object register(String plainKey, Converter converter) {
        Container c = plainKeys.get(plainKey);
        if(c == null) {
            c = new Container();
            c.plainKey = plainKey;
            c.converter = converter;
            c.objectKey = new Object();

            plainKeys.put(plainKey, c);
            objKeys.put(c.objectKey, c);
        }

        return c.objectKey;
    }

    /**
     * @deprecated
     */
    public static void registerWithObjectKey(String plainKey, Object objKey, Converter converter) {
        Container c = new Container();
        c.plainKey = plainKey;
        c.objectKey = objKey;
        c.converter = converter;

        if (plainKeys.put(plainKey, c) != null) throw new RuntimeException("You are overwriting a plain-object " +
                "key-pair!");
        if (objKeys.put(objKey, c) != null) throw new RuntimeException("You are overwriting a plain-object " +
                "key-pair!");

    }

    public static String getPlainKey(Object key) {
        Container c = objKeys.get(key);
        if (c != null) return c.plainKey;
        else return null;
    }

    public static Object getObjectKey(String key) {
        Container c = plainKeys.get(key);
        if (c != null) return c.objectKey;
        else return null;
    }

    public static String toString(Object key, Object value) {
        Container c = objKeys.get(key);
        return c.converter.toString(value);
    }

    public static Object toObject(String key, String value) {
        Container c = plainKeys.get(key);
        return c.converter.toObject(value);
    }

    private static class Container {

        private String plainKey;

        private Object objectKey;

        private Converter converter;

    }
}
