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

package de.dbanalytics.spic.mid2008HH.sim;

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.sim.ValueGenerator;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.CachedSegment;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.ProgressLogger;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author johannes
 */
public class SegmentedFacilityGenerator implements ValueGenerator {

    private static final Logger logger = Logger.getLogger(SegmentedFacilityGenerator.class);

    private final Object LOCAL_FACILITIES_KEY = new Object();

    private final ActivityFacilities facilities;

    private final FacilityData facilityData;

    private final ZoneCollection zones;

    private final Map<String, Map<Zone, List<ActivityFacility>>> typeMap;

    private final List<ActivityFacility> allFacilities;
    private final double threshold = 50000;
    private final Random random;
    private final List<String> blacklist;
    private double localProba = 0.5;

    public SegmentedFacilityGenerator(DataPool dataPool, String layer, Random random) {
        facilityData = (FacilityData) dataPool.get(FacilityDataLoader.KEY);
        facilities = facilityData.getAll();
        ZoneData zoneData = (ZoneData) dataPool.get(ZoneDataLoader.KEY);
        zones = zoneData.getLayer(layer);
        this.random = random;
        typeMap = new HashMap<>();

        allFacilities = new ArrayList(facilities.getFacilities().values());

        blacklist = new ArrayList<>();
    }

    public void addToBlacklist(String type) {
        blacklist.add(type);
    }

    public void setLocalSegmentProbability(double proba) {
        this.localProba = proba;
    }

    private Map<Zone, List<ActivityFacility>> buildSegments(String type) {
        logger.debug("Initializing facility segments...");

        final QuadTree<ActivityFacility> spatialIndex = facilityData.getQuadTree(type);
        final Map<Zone, List<ActivityFacility>> map = new ConcurrentHashMap<>();

        int n = Executor.getFreePoolSize();
        n = Math.max(n, 1);
        List<Zone>[] segments = org.matsim.contrib.common.collections.CollectionUtils.split(zones.getZones(), n);

        List<Runnable> threads = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            threads.add(new RunThread(segments[i], spatialIndex, map, threshold));
        }

        ProgressLogger.init(zones.getZones().size(), 2, 10);
        Executor.submitAndWait(threads);
        ProgressLogger.terminate();

        return map;
    }

    @Override
    public Object newValue(CachedElement act) {
        CachedPerson person = (CachedPerson) ((CachedSegment) act).getEpisode().getPerson();

        String type = act.getAttribute(CommonKeys.ACTIVITY_TYPE);
        boolean ignore = true;
        if (type != null) {
            ignore = blacklist.contains(type);
        }

        if (!ignore) {
            Map<String, List<ActivityFacility>> typedFacils = (Map<String, List<ActivityFacility>>) person.getData(LOCAL_FACILITIES_KEY);
            if (typedFacils == null) {
                typedFacils = new IdentityHashMap<>();
                person.setData(LOCAL_FACILITIES_KEY, typedFacils);
            }

            List<ActivityFacility> facils = typedFacils.get(type);
            if (facils == null) {
                facils = initLocalFacilities(act);
                typedFacils.put(type, facils);
            }

            ActivityFacility facility;
            if (random.nextDouble() > localProba) {
                facility = facilityData.randomFacility(type);
            } else {
                facility = facils.get(random.nextInt(facils.size()));
            }
            return facility;

        } else {
            return null;
        }
    }

    private List<ActivityFacility> initLocalFacilities(CachedElement act) {
        CachedPerson person = (CachedPerson) ((CachedSegment) act).getEpisode().getPerson();
        ActivityFacility home = PersonAttributeUtils.getHomeFacility(person, facilities);

        if (home == null) {
            return allFacilities;
        } else {
            String type = act.getAttribute(CommonKeys.ACTIVITY_TYPE);

            Map<Zone, List<ActivityFacility>> zoneMap = typeMap.get(type);
            if (zoneMap == null) {
                zoneMap = buildSegments(type);
                typeMap.put(type, zoneMap);
            }


            Zone zone = zones.get(new Coordinate(home.getCoord().getX(), home.getCoord().getY()));
            /*
            Zone may be null if zones used for distributing home locations is different.
            Fallback to random facility.
             */
            if (zone == null) return allFacilities;

            return zoneMap.get(zone);
        }
    }

    private static class RunThread implements Runnable {

        private final List<Zone> zones;

        private final QuadTree<ActivityFacility> spatialIndex;

        private final Map<Zone, List<ActivityFacility>> map;

        private final double threshold;

        public RunThread(List<Zone> zones, QuadTree<ActivityFacility> spatialIndex, Map<Zone, List<ActivityFacility>> map, double threshold) {
            this.zones = zones;
            this.spatialIndex = spatialIndex;
            this.map = map;
            this.threshold = threshold;
        }

        @Override
        public void run() {
            for (Zone zone : zones) {
                double x = zone.getGeometry().getCentroid().getX();
                double y = zone.getGeometry().getCentroid().getY();
                List<ActivityFacility> local = new ArrayList<>(spatialIndex.getDisk(x, y, threshold));
                map.put(zone, local);

                ProgressLogger.step();
            }
        }
    }
}
