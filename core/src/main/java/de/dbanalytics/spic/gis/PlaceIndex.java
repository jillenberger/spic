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
        this.places = Collections.unmodifiableSet(places);
        spatialActivityIndex = new HashMap<>();
    }

    public Set<Place> get() {
        return places;
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

    private synchronized void initIdIndex() {
        if (idIndex == null) {
            Map<String, Place> tmp = new HashMap<>(places.size());
            for (Place place : places) tmp.put(place.getId(), place);
            idIndex = tmp;
        }
    }

    private synchronized void initActivityIndex() {
        if (activityIndex == null) {
            Map<String, Set<Place>> tmp = new HashMap<>();
            for (Place place : places) {
                for (String activity : place.getActivities()) {
                    Set<Place> set = tmp.get(activity);
                    if (set == null) {
                        set = new HashSet<>();
                        tmp.put(activity, set);
                    }
                    set.add(place);
                }
            }

            activityIndex = tmp;
        }
    }

    private synchronized void initAttributeIndex() {
        if (activityIndex == null) attributeIndex = new AttributableIndex(places);
    }

    private synchronized void initSpatialIndex() {
        if (spatialIndex == null) spatialIndex = createSpatialIndex(places);
    }

    private synchronized SpatialIndex<Place> initSpatialActivityIndex(String activity) {
        SpatialIndex<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) {
            Set<Place> set = getForActivity(activity);
            if (set == null) set = new HashSet<>();
            tree = createSpatialIndex(set);
            spatialActivityIndex.put(activity, tree);
        }
        return tree;
    }

    private SpatialIndex<Place> createSpatialIndex(Collection<Place> places) {
        return new RTreeWrapper<>(places);
    }
}
