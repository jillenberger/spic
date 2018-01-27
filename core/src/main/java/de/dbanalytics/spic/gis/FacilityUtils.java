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
import de.dbanalytics.spic.util.Executor;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.common.collections.CollectionUtils;
import org.matsim.contrib.common.util.ProgressLogger;
import org.matsim.core.network.NetworkUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacilitiesImpl;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;

import java.util.*;

/**
 * @author johannes
 */
public class FacilityUtils {

    public static Map<Feature, List<ActivityFacility>> mapFacilities2Zones(ZoneIndex zoneCollection, ActivityFacilities facilities) {
        int nThreads = Executor.getFreePoolSize();
        /*
        Split activities into separate lists.
         */
        List<? extends ActivityFacility>[] segments = CollectionUtils.split(facilities.getFacilities().values(), nThreads);
        /*
        Initialize threads.
         */
        List<ThreadMapFacilities> threads = new ArrayList<>(nThreads);
        for (int i = 0; i < nThreads; i++) {
            threads.add(new ThreadMapFacilities(segments[i], zoneCollection));
        }
        /*
        Run threads.
         */
        ProgressLogger.init(facilities.getFacilities().size(), 2, 10);
        Executor.submitAndWait(threads);
        ProgressLogger.terminate();
        /*
        Merge results.
         */
        Set<Feature> zones = zoneCollection.get();
        Map<Feature, List<ActivityFacility>> map = new HashMap<>();
        for (Feature zone : zones) {
            List<ActivityFacility> mergeList = new ArrayList<>();
            for (int i = 0; i < nThreads; i++) {
                List<ActivityFacility> list = threads.get(i).getMap().get(zone);
                if (list != null) mergeList.addAll(list);
            }
            if(!mergeList.isEmpty()) map.put(zone, mergeList);
        }

        return map;
    }

    private static class ThreadMapFacilities implements Runnable {

        private List<? extends ActivityFacility> facilities;

        private ZoneIndex zoneCollection;

        private Map<Feature, List<ActivityFacility>> map;

        public ThreadMapFacilities(List<? extends ActivityFacility> facilities, ZoneIndex zoneCollection) {
            this.facilities = facilities;
            this.zoneCollection = zoneCollection;
        }

        public Map<Feature, List<ActivityFacility>> getMap() {
            return map;
        }

        @Override
        public void run() {
            map = new HashMap<>();

            for (ActivityFacility f : facilities) {
                Coordinate c = new Coordinate(f.getCoord().getX(), f.getCoord().getY());
                Feature zone = zoneCollection.get(c);
                if (zone != null) {
                    List<ActivityFacility> list = map.get(zone);
                    if (list == null) {
                        list = new ArrayList<>();
                        map.put(zone, list);
                    }
                    list.add(f);
                }

                ProgressLogger.step();
            }
        }
    }

    public static void connect2Network(ActivityFacilitiesImpl facilities, Network network) {
        int nThreads = Executor.getFreePoolSize();
        List<? extends ActivityFacility>[] segments = CollectionUtils.split(facilities.getFacilities().values(), nThreads);
        List<ThreadConnectFacilities> threads = new ArrayList<>(nThreads);
        for(int i = 0; i < nThreads; i++) {
            threads.add(new ThreadConnectFacilities(segments[i], network));
        }

        Executor.submitAndWait(threads);
    }

    private static class ThreadConnectFacilities implements Runnable {

        private List<? extends ActivityFacility> facilities;

        private Network network;

        public ThreadConnectFacilities(List<? extends ActivityFacility> facilities, Network network) {
            this.facilities = facilities;
            this.network = network;
        }

        @Override
        public void run() {
            for(ActivityFacility facility : facilities) {
                Link link = NetworkUtils.getNearestLink(network, facility.getCoord());
                ((ActivityFacilityImpl)facility).setLinkId(link.getId());
            }
        }
    }
}
