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
package de.dbanalytics.spic.gis;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jillenberger
 */
public class ZoneData {

    public static final String POPULATION_KEY = "population";

    public static final String NAME_KEY = "name";

    private final Map<String, ZoneCollection> layers;

    public ZoneData() {
        layers = new HashMap<>();
    }

    public ZoneCollection getLayer(String name) {
        return layers.get(name);
    }

    ZoneCollection addLayer(ZoneCollection zones, String name) {
        return layers.put(name, zones);
    }
}
