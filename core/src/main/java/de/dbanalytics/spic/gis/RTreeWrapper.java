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
import com.github.davidmoten.rtree.geometry.Rectangle;
import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class RTreeWrapper<T extends Feature> implements SpatialIndex<T> {

    private RTree<IndexEntry, com.github.davidmoten.rtree.geometry.Geometry> tree;

    public RTreeWrapper(Collection<T> features) {
        tree = RTree.star().create();

        for (T feature : features) {
            Envelope env = feature.getGeometry().getEnvelopeInternal();
            tree = tree.add(new IndexEntry(feature), Geometries.rectangle(
                    env.getMinX(),
                    env.getMinY(),
                    env.getMaxX(),
                    env.getMaxY()));
        }
    }

    @Override
    public List<T> queryContains(Coordinate coordinate) {
        List<T> result = new ArrayList<>();
        tree.search(Geometries.point(coordinate.x, coordinate.y)).
                filter(entry -> entry.value().contains(coordinate)).
                forEach(entry -> result.add(entry.value().feature));
        return result;
    }

    @Override
    public List<T> queryInside(Geometry geometry) {
        List<T> result = new ArrayList<>();
        PreparedGeometry prepGeometry = PreparedGeometryFactory.prepare(geometry);
        Envelope env = geometry.getEnvelopeInternal();
        Rectangle rect = Geometries.rectangle(
                env.getMinX(),
                env.getMinY(),
                env.getMaxX(),
                env.getMaxY());
        tree.search(rect).
                filter(entry -> prepGeometry.contains(entry.value().feature.getGeometry())).
                forEach(entry -> result.add(entry.value().feature));
        return result;
    }

    private class IndexEntry {

        private final T feature;

        private IndexedPointInAreaLocator locator;

        private IndexEntry(T feature) {
            this.feature = feature;
        }

        private boolean contains(Coordinate c) {
            if (locator == null) locator = new IndexedPointInAreaLocator(feature.getGeometry());
            return locator.locate(c) == Location.INTERIOR;
        }
    }
}
