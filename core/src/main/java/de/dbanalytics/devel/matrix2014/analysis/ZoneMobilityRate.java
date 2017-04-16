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

package de.dbanalytics.devel.matrix2014.analysis;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneData;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class ZoneMobilityRate implements AnalyzerTask<Collection<? extends Person>> {

    private Predicate<Segment> predicate;

    private final String categoryKey;

    private final TObjectDoubleHashMap<String> categoryPop;

    private TObjectDoubleHashMap categoryTrips;

    private TObjectDoubleHashMap categoryPersons;

    private TObjectDoubleHashMap<String> categoryMobilityRate;

    private final FileIOContext ioContext;

    public ZoneMobilityRate(String categoryKey, ZoneCollection zones, Predicate<Segment> predicate) {
        this(categoryKey, zones, predicate, null);
    }

    public ZoneMobilityRate(String categoryKey, ZoneCollection zones, Predicate<Segment> predicate, FileIOContext ioContext) {
        this.ioContext = ioContext;
        this.categoryKey = categoryKey;
        setPredicate(predicate);

        categoryPop = new TObjectDoubleHashMap();

        for (Zone zone : zones.getZones()) {
            String popVal = zone.getAttribute(ZoneData.POPULATION_KEY);
            String category = zone.getAttribute(categoryKey);

            if (popVal != null && category != null) {
                double inhabitants = Double.parseDouble(popVal);
                categoryPop.adjustOrPutValue(category, inhabitants, inhabitants);
            }
        }
    }

    public void setPredicate(Predicate<Segment> predicate) {
        this.predicate = predicate;
    }

    public TObjectDoubleHashMap<Zone> getMobilityRatePerZone(ZoneCollection zones) {
        TObjectDoubleHashMap<Zone> rates = new TObjectDoubleHashMap<>();
        for(Zone zone : zones.getZones()) {
            String category = zone.getAttribute(categoryKey);
            double rate = categoryMobilityRate.get(category);
            if(rate > 0) {
                rates.put(zone, rate);
            } else {
                throw new RuntimeException("Mobility rate = 0 not allowed");
            }
        }

        return rates;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {

        categoryTrips = new TObjectDoubleHashMap();
        categoryPersons = new TObjectDoubleHashMap();

        for (Person person : persons) {
            String category = person.getAttribute(categoryKey);
            Double weight = Double.parseDouble(person.getAttribute(CommonKeys.PERSON_WEIGHT));
            if (category != null) {
                double personTrips = 0;
                for (Episode episode : person.getEpisodes()) {
                    int episodeTrips = 0;
                    for (Segment leg : episode.getLegs()) {
                        if (predicate == null || predicate.test(leg)) {
                            episodeTrips++;
                        }
                    }

                    personTrips += episodeTrips;

                    if (episodeTrips > 0) {
//                        categoryPersons.adjustOrPutValue(category, 1, 1);
                        categoryPersons.adjustOrPutValue(category, weight, weight);
                    }
                }

                personTrips = personTrips * weight;
                categoryTrips.adjustOrPutValue(category, personTrips, personTrips);
            }
        }

        categoryMobilityRate = new TObjectDoubleHashMap<>();
        TObjectDoubleIterator<String> it = categoryPop.iterator();
        for (int i = 0; i < categoryPop.size(); i++) {
            it.advance();
            double population = it.value();
            double mobPersons = categoryPersons.get(it.key());
            categoryMobilityRate.put(it.key(), mobPersons / population);
        }

        if (ioContext != null) {
            try {
                String file = String.format("%s/%s.trips.txt", ioContext.getPath(), categoryKey);
                StatsWriter.writeLabeledHistogram(categoryTrips, categoryKey, "trips", file);

                file = String.format("%s/%s.population.txt", ioContext.getPath(), categoryKey);
                StatsWriter.writeLabeledHistogram(categoryPop, categoryKey, "population", file);

                file = String.format("%s/%s.mobilePop.txt", ioContext.getPath(), categoryKey);
                StatsWriter.writeLabeledHistogram(categoryPersons, categoryKey, "mobilePop", file);

                file = String.format("%s/%s.mobilityRate.txt", ioContext.getPath(), categoryKey);
                StatsWriter.writeLabeledHistogram(categoryMobilityRate, categoryKey, "mobilityRate", file);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
