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
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import de.dbanalytics.spic.data.AttributableIndex;
import org.matsim.core.utils.collections.QuadTree;

import java.util.*;

/**
 * @author jillenberger
 */
public class PlaceIndex {

    private final Set<Place> places;

    private AttributableIndex attributeIndex;

    private QuadTree<Place> spatialIndex;

    private Map<String, QuadTree<Place>> spatialActivityIndex;

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
        return new LinkedHashSet<>(queryInside(geometry, spatialIndex));
    }

    public List<Place> get(Coordinate center, double r_min, double r_max) {
        if (spatialIndex == null) initSpatialIndex();
        return new ArrayList<>(spatialIndex.getRing(center.x, center.y, r_min, r_max));
    }

    public List<Place> getForActivity(Coordinate center, double r_min, double r_max, String activity) {
        QuadTree<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);

        ArrayList<Place> result = (ArrayList<Place>) tree.getRing(center.x, center.y, r_min, r_max);

        return result;
    }

    public Set<Place> getForActivity(Geometry geometry, String activity) {
        QuadTree<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);
        return new LinkedHashSet<>(queryInside(geometry, tree));
    }

    public Place getClosestForActivity(Coordinate center, String activity) {
        QuadTree<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) tree = initSpatialActivityIndex(activity);

        return tree.getClosest(center.x, center.y);
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

    private synchronized QuadTree<Place> initSpatialActivityIndex(String activity) {
        QuadTree<Place> tree = spatialActivityIndex.get(activity);
        if (tree == null) {
            List<Place> list = getForActivity(activity);
            if (list == null) list = new ArrayList<>();
            tree = createSpatialIndex(list);
            spatialActivityIndex.put(activity, tree);
        }
        return tree;
    }

    private QuadTree<Place> createSpatialIndex(Collection<Place> places) {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double maxx = 0;
        double maxy = 0;

        for (Place fac : places) {
            minx = Math.min(minx, fac.getGeometry().getCoordinate().x);
            miny = Math.min(miny, fac.getGeometry().getCoordinate().y);
            maxx = Math.max(maxx, fac.getGeometry().getCoordinate().x);
            maxy = Math.max(maxy, fac.getGeometry().getCoordinate().y);
        }

        QuadTree<Place> tree = new QuadTree<>(minx, miny, maxx, maxy);

        for (Place fac : places) {
            tree.put(fac.getGeometry().getCoordinate().x, fac.getGeometry().getCoordinate().y, fac);
        }

        return tree;
    }

    private List<Place> queryInside(Geometry geometry, QuadTree<Place> tree) {

        PreparedGeometry prepGeometry = PreparedGeometryFactory.prepare(geometry);
        Envelope env = geometry.getEnvelopeInternal();

        List<Place> candidates = new ArrayList<>(1000);
        tree.getRectangle(env.getMinX(), env.getMinY(), env.getMaxX(), env.getMaxY(), candidates);
        List<Place> result = new ArrayList<>(1000);
        for (Place place : candidates) {
            if (prepGeometry.contains(place.getGeometry())) {
                result.add(place);
            }
        }

        return result;
    }
}
