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

package de.dbanalytics.spic.data;

import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author jillenberger
 */
public class AttributableIndex<T extends Attributable> {

    private static final String NULL_VALUE = "";

    private final Set<T> attributables;

    private final Map<Pair<String, String>, Set<T>> index;

    private final Set<Pair<String, String>> compoundKeys;

    public AttributableIndex(Set<T> attributables) {
        this.attributables = attributables;
        index = new HashMap<>();
        compoundKeys = new HashSet<>();
    }

    public Set<T> get(@NotNull String key, String value) {
        if (value == null) value = NULL_VALUE;

        Pair<String, String> compoundKey = new ImmutablePair<>(key, value);
        if (compoundKeys.add(compoundKey)) init(key);

        Set<T> result = index.get(compoundKey);
        if (result == null) result = new HashSet<>();
        return result;
    }

    private void init(String key) {
        for (T attributable : attributables) {
            String value = attributable.getAttribute(key);
            if (value == null) value = NULL_VALUE;

            Pair<String, String> compoundKey = new ImmutablePair<>(key, value);

            Set<T> result = index.get(compoundKey);
            if (result == null) {
                result = new HashSet<>();
                index.put(compoundKey, result);
            }
            result.add(attributable);
        }
    }
}
