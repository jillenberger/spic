/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.gis;

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.spic.data.AttributableIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by johannesillenberger on 30.05.17.
 */
public class ZoneIndex {

    private final Set<Feature> zones;

    private Map<String, Feature> idIndex;

    private SpatialIndex<Feature> spatialIndex;

    private AttributableIndex attributableIndex;

    public ZoneIndex(Set<Feature> zone) {
        this.zones = zone;
    }

    public Set<Feature> get() {
        return zones;
    }

    public Feature get(String id) {
        if (idIndex == null) initIdIndex();
        return idIndex.get(id);
    }

    public Feature get(Coordinate coordinate) {
        if (spatialIndex == null) initSpatialIndex();

        List<Feature> candidates = spatialIndex.queryContains(coordinate);
        if (candidates.isEmpty()) return null;
        else return candidates.get(0);
    }

    public Set<Feature> get(String key, String value) {
        if (attributableIndex == null) initAttributableIndex();

        return attributableIndex.get(key, value);
    }

    private synchronized void initIdIndex() {
        if (idIndex == null) {
            idIndex = new HashMap<>(zones.size());
            zones.stream().forEach(zone -> idIndex.put(zone.getId(), zone));
        }
    }

    private synchronized void initSpatialIndex() {
        if (spatialIndex == null) spatialIndex = new RTreeWrapper<>(zones);
    }

    private synchronized void initAttributableIndex() {
        if (attributableIndex == null) attributableIndex = new AttributableIndex(zones);
    }
}
