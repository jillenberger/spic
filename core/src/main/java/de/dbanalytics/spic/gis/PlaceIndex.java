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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.dbanalytics.spic.data.AttributableIndex;
import org.geotools.geometry.jts.JTSFactoryFinder;

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

    private Map<String, List<Place>> activityIndex;

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

    public List<Place> getForActivity(String activity) {
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

    public List<Place> getForActivity(Coordinate center, double r_min, double r_max, String activity) {
        SpatialIndex<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);

        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();

        Envelope env = new Envelope(center.x - r_max,
                center.x + r_max,
                center.y - r_min,
                center.y + r_max);
        List<Place> places = tree.queryInside(factory.toGeometry(env));
        places.stream().filter(place -> {
            double dx = center.x - place.getGeometry().getCoordinate().x;
            double dy = center.y - place.getGeometry().getCoordinate().y;
            double d = Math.sqrt(dx * dx + dy * dy);

            return (d >= r_min && d <= r_max);
        });
        return places;
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
            Map<String, List<Place>> tmp = new HashMap<>();
            for (Place place : places) {
                for (String activity : place.getActivities()) {
                    List<Place> list = tmp.get(activity);
                    if (list == null) {
                        list = new ArrayList<>();
                        tmp.put(activity, list);
                    }
                    list.add(place);
                }
            }

            for (List<Place> list : tmp.values()) ((ArrayList) list).trimToSize();

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
            List<Place> list = getForActivity(activity);
            if (list == null) list = new ArrayList<>();
            tree = createSpatialIndex(list);
            spatialActivityIndex.put(activity, tree);
        }
        return tree;
    }

    private SpatialIndex<Place> createSpatialIndex(Collection<Place> places) {
        return new RTreeWrapper<>(places);
    }
}
