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

import com.vividsolutions.jts.algorithm.locate.IndexedPointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author johannes
 */
public class ZoneCollection {

    private static final Logger logger = Logger.getLogger(ZoneCollection.class);

    private final String id;

    private final Set<Zone> zones;

    private SpatialIndex spatialIndex;

    private Map<String, Zone> keyIndex;

    private String primaryKey;

    public ZoneCollection(String id) {
        this.id = id;
        zones = new LinkedHashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String key) {
        this.primaryKey = key;
        buildKeyIndex();
    }

    public void add(Zone zone) {
        this.zones.add(zone);
        buildIndex();
    }

    public void addAll(Collection<Zone> zones) {
        this.zones.addAll(zones);
        buildIndex();
    }

    private void buildIndex() {
        buildKeyIndex();
        buildSpatialIndex();
    }

    private void buildKeyIndex() {
        keyIndex = new HashMap<>();
        if (primaryKey != null) {
            for (Zone zone : zones) {
                String key = zone.getAttribute(primaryKey);
                if (key == null)
                    throw new NullPointerException();
                if (null != keyIndex.put(key, zone)) {
                    logger.warn(String.format("Overwriting key %s.", zone.getAttribute(primaryKey)));
//					throw new RuntimeException("Overwriting key " + zone.getAttribute(primaryKey));
                }
            }
        }
    }

    private void buildSpatialIndex() {
        //TODO check SRID fields
        //TODO check for overlapping polygons

        STRtree tree = new STRtree();
        for (Zone zone : zones) {
            tree.insert(zone.getGeometry().getEnvelopeInternal(), new IndexEntry(zone));
        }

        tree.build();

        spatialIndex = tree;

    }

    public Set<Zone> getZones() {
        return Collections.unmodifiableSet(zones);
    }

    public Zone get(String key) {
        return keyIndex.get(key);
    }

    public Zone get(Coordinate c) {
        List<IndexEntry> candidates = spatialIndex.query(new Envelope(c));

//		if (candidates.size() == 1) {
//			return candidates.get(0).zone;
//		}

        for (IndexEntry entry : candidates) {
            if (entry.contains(c)) {
                return entry.zone;
            }
        }

        return null;
    }

    private class IndexEntry {

        private final Zone zone;
        private IndexedPointInAreaLocator locator;

        private IndexEntry(Zone zone) {
            this.zone = zone;
        }

        private boolean contains(Coordinate c) {
            if (locator == null) {
                locator = new IndexedPointInAreaLocator(zone.getGeometry());
            }

            return locator.locate(c) == Location.INTERIOR;
        }
    }
}
