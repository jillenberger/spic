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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.source.mid2008HH;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.*;

/**
 * Created by johannesillenberger on 04.05.17.
 */
public class HomeLocator {

    private static final Logger logger = Logger.getLogger(HomeLocator.class);

    private final Random random;

    private final PlaceIndex placeIndex;

    private final ZoneCollection zones;
    private String partitionKey;
    private String inhabitantsKey;

    public HomeLocator(PlaceIndex placeIndex, ZoneCollection zones) {
        this(placeIndex, zones, new XORShiftRandom());
    }

    public HomeLocator(PlaceIndex placeIndex, ZoneCollection zones, Random random) {
        this.placeIndex = placeIndex;
        this.zones = zones;
        this.random = random;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public void setInhabitantsKey(String inhabitantsKey) {
        this.inhabitantsKey = inhabitantsKey;
    }

    public Set<Person> run(Set<Person> refPersons, double fraction) {
        AttributableIndex<Person> personIndex = new AttributableIndex<>(refPersons);
        Set<Person> targetPersons = new HashSet<>();
        /**
         * Sort zones according to attribute.
         */
        Map<String, Set<Zone>> zoneIndex = new HashMap<>();
        for (Zone zone : zones.getZones()) {
            String value = zone.getAttribute(partitionKey);
            Set<Zone> partition = zoneIndex.get(value);
            if (partition == null) {
                partition = new HashSet<>();
                zoneIndex.put(value, partition);
            }
            partition.add(zone);
        }
        /**
         * Process partitions.
         */
        for (Map.Entry<String, Set<Zone>> entry : zoneIndex.entrySet()) {
            logger.info(String.format("Processing partition %s...", entry.getKey()));

            Set<Person> templates = personIndex.get(MiDHHValues.PERSON_DISTRICT, entry.getKey());
            Set<Zone> partition = entry.getValue();
            /**
             * Total number inhabitants in partition
             */
            int numPartition = 0;
            for (Zone zone : partition) {
                String value = zone.getAttribute(inhabitantsKey);
                if (value != null) numPartition += (int) Double.parseDouble(value);
            }
            numPartition = (int) Math.ceil(numPartition * fraction);

            if (numPartition > 0) {
                List<Person> clones = new ArrayList<>(PersonUtils.weightedCopy(
                        templates,
                        new PlainFactory(),
                        numPartition,
                        random));

                int startIdx = 0;
                for (Zone zone : partition) {
                    int numZone = 0;

                    String value = zone.getAttribute(inhabitantsKey);
                    if (value != null) numZone = (int) Double.parseDouble(value);
                    numZone = (int) Math.floor(numZone * fraction);

                    if (numZone > 0) {
                        List<Place> homePlaces = new ArrayList<>(placeIndex.getForActivity(zone.getGeometry(), "home"));
                        if (homePlaces.size() > 0) {
                            int endIdx = startIdx + numZone;
                            for (int i = startIdx; i < endIdx; i++) {
                                Person clone = clones.get(i);
                                Place home = homePlaces.get(random.nextInt(homePlaces.size()));
                                clone.getEpisodes().stream().forEach(episode -> episode.getActivities().
                                        stream().
                                        filter(activity ->
                                                ActivityTypes.HOME.equalsIgnoreCase(activity.getAttribute(CommonKeys.ACTIVITY_TYPE))).
                                        forEach(activity ->
                                                activity.setAttribute(CommonKeys.ACTIVITY_FACILITY, home.getId()))
                                );
                                targetPersons.add(clone);
                            }
                            startIdx = endIdx;
                        } else {
                            logger.warn(String.format("No home places for zone with %s persons.", numZone));
                        }
                    }
                }

                if (startIdx != numPartition) {
                    logger.warn(String.format("Cloned %s persons but target amount is %s (%.4f).",
                            startIdx,
                            numPartition,
                            startIdx / (double) numPartition));
                }
            } else {
                logger.warn("No inhabitants in partition.");
            }

        }

        return targetPersons;
    }
}
