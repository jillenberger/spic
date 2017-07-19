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
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author johannes
 */
public class DefaultMatrixBuilder implements MatrixBuilder {

    private static final Logger logger = Logger.getLogger(DefaultMatrixBuilder.class);

    private final PlaceIndex placeIndex;

//    private final String zoneIdKey;

    private final ZoneCollection zones;

    private Predicate<Segment> legPredicate;

    private boolean useWeights;

    public DefaultMatrixBuilder(PlaceIndex placeIndex, ZoneCollection zones) {
        this.placeIndex = placeIndex;
        this.zones = zones;
//        zoneIdKey = zones.getId() + "_zone_id";
    }

    public void setLegPredicate(Predicate<Segment> predicate) {
        this.legPredicate = predicate;
    }

    public void setUseWeights(boolean useWeights) {
        this.useWeights = useWeights;
    }

    @Override
    public NumericMatrix build(Collection<? extends Person> persons) {
        logger.debug("Start building matrix...");
        int n = persons.size() / 10000;
        n = Math.min(n, Executor.getFreePoolSize());
        n = Math.max(1, n);
        List<? extends Person>[] segments = org.matsim.contrib.common.collections.CollectionUtils.split(persons, n);

        List<RunThread> runnables = new ArrayList<>(n);
        for(List<? extends Person> segment : segments) {
            runnables.add(new RunThread(segment, legPredicate, useWeights));
        }

        Executor.submitAndWait(runnables);

        int countNoZones = 0;
        int countNullIds = 0;
        int size = 0;
        Set<NumericMatrix> matrices = new HashSet<>();
        for(RunThread runnable : runnables) {
            matrices.add(runnable.getMatrix());
            countNoZones += runnable.getCountNoZones();
            countNullIds += runnable.getCountNullIds();
            size += runnable.getSize();
        }
        NumericMatrix m = new NumericMatrix();
        MatrixOperations.accumulate(matrices, m);

        if (countNoZones > 0) {
            logger.warn(String.format(Locale.US, "%s of %s (%.2f %%) od-pairs skipped because at least one facility cannot be located in a zone.",
                    countNoZones,
                    size + countNoZones,
                    countNoZones / ((double) size + countNoZones) * 100));
        }

        if (countNullIds > 0) {
            logger.warn(String.format("%s null facilities ids.", countNullIds));
        }

        logger.debug("Done building matrix.");
        return m;
    }

    public class RunThread implements Runnable {

        private final Collection<? extends Person> persons;

        private final Map<String, String> zoneIds;

        private final Predicate<Segment> predicate;

        private final NumericMatrix m;

        private final boolean useWeights;

        private int countNoZones;

        private int countNullIds;

        private int size;

        public RunThread(Collection<? extends Person> persons, Predicate<Segment> predicate, boolean useWeights) {
            this.persons = persons;
            this.predicate = predicate;
            this.useWeights = useWeights;

            zoneIds = new HashMap<>();

            m = new NumericMatrix();
        }

        public NumericMatrix getMatrix() {
            return m;
        }

        public int getCountNoZones() {
            return countNoZones;
        }

        public int getCountNullIds() {
            return countNullIds;
        }

        private int getSize() {
            return size;
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

                            String originPlaceId = prev.getAttribute(CommonKeys.ACTIVITY_FACILITY);
                            String destPlaceId = next.getAttribute(CommonKeys.ACTIVITY_FACILITY);

                            if (originPlaceId != null && destPlaceId != null) {
                                String origin = getZoneId(originPlaceId);
                                String dest = getZoneId(destPlaceId);

                                if (origin != null && dest != null) {
                                    double w = 1.0;
                                    if (useWeights)
                                        w = Double.parseDouble(person.getAttribute(CommonKeys.PERSON_WEIGHT));
                                    m.add(origin, dest, w);
                                    size++;
                                } else {
                                    countNoZones++;
                                }
                            } else {
                                countNullIds++;
                            }
                        }
                    }
                }
            }
        }

        private String getZoneId(String placeId) {
            if (placeId == null) {
                logger.warn("Place id must not be null!");
                return null;
            }

            String zoneId = zoneIds.get(placeId);

            if (zoneId == null) {
                Place place = placeIndex.get(placeId);
//                zoneId = place.getAttribute(zoneIdKey);

                if (zoneId == null) {
                    Zone zone = zones.get(place.getGeometry().getCoordinate());
                    if (zone != null) {
                        zoneId = zone.getAttribute(zones.getPrimaryKey());
                    } else {
                        // facility is outside bounds of zones
                        return null;
                    }
                }
                zoneIds.put(placeId, zoneId);
            }

            return zoneId;
        }
    }
}
