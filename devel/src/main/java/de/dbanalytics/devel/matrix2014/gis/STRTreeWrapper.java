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

package de.dbanalytics.devel.matrix2014.gis;

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.index.strtree.STRtree;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.SpatialIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class STRTreeWrapper<T extends Feature> implements SpatialIndex<T> {

    private STRtree tree;

    public STRTreeWrapper(Collection<T> features) {
        tree = new STRtree(features.size());
        for (T feature : features) {
            tree.insert(feature.getGeometry().getEnvelopeInternal(), new IndexEntry(feature));
        }
        tree.build();
    }

    @Override
    public List<T> queryContains(Coordinate coordinate) {
        List<IndexEntry> candidates = tree.query(new Envelope(coordinate));
        List<T> result = new ArrayList<>(candidates.size());

        for (IndexEntry entry : candidates) {
            if (entry.contains(coordinate)) {
                result.add(entry.feature);
            }
        }

        return result;
    }

    @Override
    public List<T> queryInside(Geometry geometry) {
        List<IndexEntry> candidates = tree.query(geometry.getEnvelopeInternal());
        List<T> result = new ArrayList<>(candidates.size());

        if (candidates.isEmpty()) return result;

        PreparedGeometry preGeometry = PreparedGeometryFactory.prepare(geometry);
        for (IndexEntry entry : candidates) {
            if (preGeometry.contains(entry.feature.getGeometry())) {
                result.add(entry.feature);
            }
        }

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
