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

import java.util.List;
import java.util.Set;

/**
 * Created by johannesillenberger on 30.05.17.
 */
public class ZoneIndex {

    private final Set<Feature> zones;

    private SpatialIndex<Feature> spatialIndex;

    private AttributableIndex attributableIndex;

    public ZoneIndex(Set<Feature> zone) {
        this.zones = zone;
    }

    public Set<Feature> get() {
        return zones;
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

    private void initSpatialIndex() {
        spatialIndex = new RTreeWrapper<>(zones);
    }

    private void initAttributableIndex() {
        attributableIndex = new AttributableIndex(zones);
    }
}
