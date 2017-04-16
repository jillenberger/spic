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

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.ProgressLogger;
import org.matsim.facilities.ActivityFacility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author johannes
 */
public class ValidateFacilities {

    private static final Logger logger = Logger.getLogger(ValidateFacilities.class);

    public static void validate(DataPool dataPool, String layerName) {
        logger.info(String.format("Removing facilities that are not in bounds of zones \"%s\"...", layerName));

        FacilityData facilityData = (FacilityData) dataPool.get(FacilityDataLoader.KEY);
        Collection<? extends ActivityFacility> facilities = facilityData.getAll().getFacilities().values();

        ZoneData zoneDate = (ZoneData) dataPool.get(ZoneDataLoader.KEY);
        ZoneCollection zones = zoneDate.getLayer(layerName);

        int numThreads = Executor.getFreePoolSize();
        List[] segments = org.matsim.contrib.common.collections.CollectionUtils.split(facilities, numThreads);
        List<ActivityFacility> remove = new CopyOnWriteArrayList<>();
        List<Runnable> runnables = new ArrayList<>(numThreads);
        for(List segment : segments) {
            runnables.add(new RunThread(segment, remove, zones));
        }

        ProgressLogger.init(facilities.size(), 2, 10);
        Executor.submitAndWait(runnables);
        ProgressLogger.terminate();

        for(ActivityFacility f : remove) {
//            facilities.remove(f);
            facilityData.getAll().getFacilities().remove(f.getId());
        }

        if(remove.size() > 0) {
            logger.info(String.format("Removed %s facilities.", remove.size()));
        }
    }

    private static class RunThread implements Runnable {

        private final List<ActivityFacility> remove;

        private final List<ActivityFacility> facilities;

        private final ZoneCollection zones;

        public RunThread(List<ActivityFacility> facilities, List<ActivityFacility> remove, ZoneCollection zones) {
            this.facilities = facilities;
            this.zones = zones;
            this.remove = remove;
        }

        @Override
        public void run() {
            for(ActivityFacility f : facilities) {
                Zone zone = zones.get(new Coordinate(f.getCoord().getX(), f.getCoord().getY()));
                if(zone == null) {
                    remove.add(f);
                }

                ProgressLogger.step();
            }
        }
    }
}
