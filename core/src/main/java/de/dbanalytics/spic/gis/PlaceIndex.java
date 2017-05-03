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

import com.vividsolutions.jts.geom.Geometry;
import de.dbanalytics.spic.data.AttributableIndex;

import java.util.*;

/**
 * @author jillenberger
 */
public class PlaceIndex {

    private final Set<Place> places;

    private AttributableIndex attributeIndex;

    private SpatialIndex<Place> spatialIndex;

    private Map<String, SpatialIndex<Place>> spatialActivityIndex;

    private Map<String, Place> idIndex;

    private Map<String, Set<Place>> activityIndex;

    public PlaceIndex(Set<Place> places) {
        this.places = places;
        spatialActivityIndex = new HashMap<>();
    }

    public Place get(String id) {
        if (idIndex == null) initIdIndex();
        return idIndex.get(id);
    }

    public Set<Place> getForActivity(String activity) {
        if (activityIndex == null) initActivityIndex();
        return activityIndex.get(activity);
    }

    public Set<Place> get(String key, String value) {
        if (attributeIndex == null) initAttributeIndex();
        return attributeIndex.get(key, value);
    }

    public Set<Place> get(Geometry geometry) {
        if (spatialIndex == null) initSpatialIndex();
        return new HashSet<>(spatialIndex.queryInside(geometry));
    }

    public Set<Place> getForActivity(Geometry geometry, String activity) {
        SpatialIndex<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);
        return new HashSet<>(tree.queryInside(geometry));
    }

    private void initIdIndex() {
        idIndex = new HashMap<>(places.size());
        for (Place place : places) idIndex.put(place.getId(), place);
    }

    private void initActivityIndex() {
        activityIndex = new HashMap<>();
        for (Place place : places) {
            for (String activity : place.getActivities()) {
                Set<Place> set = activityIndex.get(activity);
                if (set == null) {
                    set = new HashSet<>();
                    activityIndex.put(activity, set);
                }
                set.add(place);
            }
        }
    }

    private void initAttributeIndex() {
        attributeIndex = new AttributableIndex(places);
    }

    private void initSpatialIndex() {
        spatialIndex = createSpatialIndex(places);
    }

    private SpatialIndex<Place> initSpatialActivityIndex(String activity) {
        Set<Place> set = getForActivity(activity);
        if (set == null) set = new HashSet<>();
        SpatialIndex<Place> tree = createSpatialIndex(set);
        spatialActivityIndex.put(activity, tree);
        return tree;
    }

    private SpatialIndex<Place> createSpatialIndex(Collection<Place> places) {
        return new RTreeWrapper<>(places);
    }
}
