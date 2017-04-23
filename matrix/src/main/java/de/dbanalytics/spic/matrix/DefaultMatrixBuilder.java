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

package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.ActivityLocationLayer;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author johannes
 */
public class DefaultMatrixBuilder implements MatrixBuilder {

    private static final Logger logger = Logger.getLogger(DefaultMatrixBuilder.class);

    private final ActivityLocationLayer locationLayer;

    private final String zoneIdKey;

    private final ZoneCollection zones;

    private final Map<String, String> zoneIds;

    private Predicate<Segment> legPredicate;

    private boolean useWeights;

    public DefaultMatrixBuilder(ActivityLocationLayer locations, ZoneCollection zones) {
        this.locationLayer = locations;
        this.zones = zones;
        zoneIdKey = zones.getId() + "_zone_id";
        zoneIds = new ConcurrentHashMap<>();
    }

    public void setLegPredicate(Predicate<Segment> predicate) {
        this.legPredicate = predicate;
    }

    public void setUseWeights(boolean useWeights) {
        this.useWeights = useWeights;
    }

    private String getZoneId(String facilityId) {
        String zoneId = zoneIds.get(facilityId);

        if(zoneId == null) {
            Feature location = locationLayer.get(facilityId);
            zoneId = location.getAttribute(zoneIdKey);

            if(zoneId == null) {
                Zone zone = zones.get(location.getGeometry().getCoordinate());
                if(zone != null) {
                    zoneId = zone.getAttribute(zones.getPrimaryKey());
                } else {
                    // facility is outside bounds of zones
                    return null;
                }
            }
            zoneIds.put(facilityId, zoneId);
        }

        return zoneId;
    }

    @Override
    public NumericMatrix build(Collection<? extends Person> persons) {
        logger.debug("Start building matrix...");
        int n = persons.size() / 10000;
        n = Math.min(n, Executor.getFreePoolSize());
        //n = Math.max(2, n);
        List<? extends Person>[] segments = org.matsim.contrib.common.collections.CollectionUtils.split(persons, n);

        List<RunThread> runnables = new ArrayList<>(n);
        for(List<? extends Person> segment : segments) {
            runnables.add(new RunThread(segment, legPredicate, useWeights));
        }

        Executor.submitAndWait(runnables);

        int errors = 0;
        Set<NumericMatrix> matrices = new HashSet<>();
        for(RunThread runnable : runnables) {
            matrices.add(runnable.getMatrix());
            errors += runnable.getErrors();
        }
        NumericMatrix m = new NumericMatrix();
        MatrixOperations.accumulate(matrices, m);

        if(errors > 0) {
            logger.warn(String.format("%s facilities cannot be located in a zone.", errors));
        }
        logger.debug("Done building matrix.");
        return m;
    }

    public class RunThread implements Runnable {

        private final Collection<? extends Person> persons;

        private final Predicate<Segment> predicate;

        private final NumericMatrix m;

        private final boolean useWeights;

        private int errors;

        public RunThread(Collection<? extends Person> persons, Predicate<Segment> predicate, boolean useWeights) {
            this.persons = persons;
            this.predicate = predicate;
            this.useWeights = useWeights;

            m = new NumericMatrix();
        }

        public NumericMatrix getMatrix() {
            return m;
        }

        public int getErrors() {
            return  errors;
        }

        @Override
        public void run() {
            for(Person person : persons) {
                for (Episode episode : person.getEpisodes()) {
                    for (int i = 0; i < episode.getLegs().size(); i++) {
                        Segment leg = episode.getLegs().get(i);
                        if (predicate == null || predicate.test(leg)) {
                            Segment prev = episode.getActivities().get(i);
                            Segment next = episode.getActivities().get(i + 1);

                            String originFacId = prev.getAttribute(CommonKeys.ACTIVITY_FACILITY);
                            String origin = getZoneId(originFacId);

                            String destFacId = next.getAttribute(CommonKeys.ACTIVITY_FACILITY);
                            String dest = getZoneId(destFacId);

                            if (origin != null && dest != null) {
                                double w = 1.0;
                                if(useWeights) w = Double.parseDouble(person.getAttribute(CommonKeys.PERSON_WEIGHT));
                                m.add(origin, dest, w);
                            } else {
                                errors++;
                            }
                        }
                    }
                }
            }
        }
    }
}