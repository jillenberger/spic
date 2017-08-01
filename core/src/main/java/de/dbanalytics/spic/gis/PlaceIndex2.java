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

import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import de.dbanalytics.spic.data.AttributableIndex;

import java.util.*;

/**
 * @author jillenberger
 */
public class PlaceIndex2 {

    private final Set<Place> places;

    private AttributableIndex attributeIndex;

    private RTree<Place, Point> spatialIndex;

    private Map<String, RTree<Place, Point>> spatialActivityIndex;

    private Map<String, Place> idIndex;

    private Map<String, List<Place>> activityIndex;

    public PlaceIndex2(Set<Place> places) {
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
//        return new HashSet<>(spatialIndex.queryInside(geometry));
        return new HashSet<>(queryInside(geometry, spatialIndex));
    }

    public List<Place> getForActivity(Coordinate center, double r_min, double r_max, String activity) {
        RTree<Place, Point> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);

        ArrayList<Place> result = new ArrayList<>(1000);
        tree.search(Geometries.circle(center.x, center.y, r_max)).
                filter(entry -> {
                    double dx = center.x - entry.value().getGeometry().getCoordinate().x;
                    double dy = center.y - entry.value().getGeometry().getCoordinate().y;
                    double d = Math.sqrt(dx * dx + dy * dy);
                    return (d >= r_min && d <= r_max);
                }).
                forEach(entry -> result.add(entry.value()));
        return result;
    }

    public Set<Place> getForActivity(Geometry geometry, String activity) {
        RTree<Place, Point> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);
        return new HashSet<>(queryInside(geometry, tree));
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

    private synchronized RTree<Place, Point> initSpatialActivityIndex(String activity) {
        RTree<Place, Point> tree = spatialActivityIndex.get(activity);
        if (tree == null) {
            List<Place> list = getForActivity(activity);
            if (list == null) list = new ArrayList<>();
            tree = createSpatialIndex(list);
            spatialActivityIndex.put(activity, tree);
        }
        return tree;
    }

    private RTree<Place, Point> createSpatialIndex(Collection<Place> places) {
        RTree<Place, Point> tree = RTree.star().create();

        for (Place feature : places) {
            double x = feature.getGeometry().getCoordinate().x;
            double y = feature.getGeometry().getCoordinate().y;
            tree.add(feature, Geometries.point(x, y));
        }

        return tree;
    }

    private List<Place> queryInside(Geometry geometry, RTree<Place, Point> tree) {
        List<Place> result = new ArrayList<>();
        PreparedGeometry prepGeometry = PreparedGeometryFactory.prepare(geometry);
        Envelope env = geometry.getEnvelopeInternal();
        Rectangle rect = Geometries.rectangle(
                env.getMinX(),
                env.getMinY(),
                env.getMaxX(),
                env.getMaxY());
        tree.search(rect).
                filter(entry -> prepGeometry.contains(entry.value().getGeometry())).
                forEach(entry -> result.add(entry.value()));

        return result;
    }
}
