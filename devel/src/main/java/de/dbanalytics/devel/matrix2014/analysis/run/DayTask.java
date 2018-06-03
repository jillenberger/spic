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

package de.dbanalytics.devel.matrix2014.analysis.run;

import de.dbanalytics.devel.matrix2014.analysis.SetSeason;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class DayTask implements AnalyzerTask<Collection<? extends Person>> {

    private final FileIOContext ioContext;

    public DayTask(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        /*
        Get all purposes.
         */
        LegCollector<String> purposeCollector = new LegCollector<>(new AttributeProvider<>(CommonKeys.TRAVEL_PURPOSE));
        Set<String> purposes = new HashSet<>(purposeCollector.collect(persons));
        purposes.remove(null);
        /*
        Get all seasons
         */
        PersonCollector<String> seasonCollector = new PersonCollector<>(new AttributeProvider<>(SetSeason.SEASON_KEY));
        Set<String> seasons = new HashSet<>(seasonCollector.collect(persons));
        seasons.remove(null);
        /*
        Get all days
         */
        PersonCollector<String> dayCollector = new PersonCollector<>(new AttributeProvider<>(CommonKeys.DAY));
        Set<String> days = new HashSet<>(dayCollector.collect(persons));
        days.remove(null);

        List<Pair<Map<String, String>, TObjectDoubleMap<String>>> shareHists = new ArrayList<>();
        List<Pair<Map<String, String>, TObjectDoubleMap<String>>> absoluteHists = new ArrayList<>();

        Predicate<Segment> modePredicate = new LegAttributePredicate(CommonKeys.MODE, CommonValues.LEG_MODE_CAR);

        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        borders.add(50000);
        borders.add(100000);
        borders.add(1000000);
        borders.add(Double.MAX_VALUE);
        Discretizer discretizer = new FixedBordersDiscretizer(borders.toArray());

        for (String purpose : purposes) {
            Predicate<Segment> purposePredicate = new LegAttributePredicate(CommonKeys.TRAVEL_PURPOSE, purpose);

            for (String season : seasons) {
                Predicate<Segment> seasonPredicate = new LegPersonAttributePredicate(SetSeason.SEASON_KEY, season);

                for (int idx = 1; idx < borders.size() - 1; idx++) {
                    Predicate<Segment> distPrediate = new DistancePredicate(idx, discretizer);

                    TObjectDoubleMap<String> hist = new TObjectDoubleHashMap<>();
                    TObjectDoubleMap<String> histShare = new TObjectDoubleHashMap<>();

                    for (String day : days) {
                        Predicate<Segment> dayPredicate = new LegPersonAttributePredicate(CommonKeys.DAY, day);

                        Predicate<Segment> predicate = PredicateAndComposite.create(modePredicate,
                                purposePredicate,
                                seasonPredicate,
                                distPrediate,
                                dayPredicate);

                        LegPersonCollector<Double> counter = new LegPersonCollector(new NumericAttributeProvider<>(CommonKeys.WEIGHT));
                        counter.setPredicate(predicate);
                        List<Double> weights = counter.collect(persons);
                        hist.put(day, sum(weights));
                        histShare.put(day, sum(weights));

//                        LegCollector<Segment> counter = new LegCollector(new EntityProvider());
//                        counter.setPredicate(predicate);
//
//                        hist.put(day, counter.collect(persons).size());
                    }

                    Map<String, String> dimensions = new HashMap<>();
                    dimensions.put(CommonKeys.MODE, CommonValues.LEG_MODE_CAR);
                    dimensions.put(CommonKeys.TRAVEL_PURPOSE, purpose);
                    dimensions.put(SetSeason.SEASON_KEY, season);
                    dimensions.put(CommonKeys.BEELINE_DISTANCE, String.valueOf(borders.get(idx)));

                    absoluteHists.add(new ImmutablePair<>(dimensions, hist));
                    Histogram.normalize(histShare);
                    shareHists.add(new ImmutablePair<>(dimensions, histShare));
                }
            }
        }

        if(ioContext != null) {
            write(absoluteHists, "day.txt");
            write(shareHists, "day.share.txt");
        }
    }

    private double sum(List<Double> values) {
        double sum = 0;
        for(Double d : values) sum += d;
        return sum;
    }

    private void write(List<Pair<Map<String, String>, TObjectDoubleMap<String>>> values, String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s/%s", ioContext.getPath(), filename)));

            if (!values.isEmpty()) {

                Set<String> predKeys = values.get(0).getLeft().keySet();
                for (String key : predKeys) {
                    writer.write(key);
                    writer.write("\t");
                }

                Set<String> dayKeys = values.get(0).getRight().keySet();
                for(String key : dayKeys) {
                    writer.write(key);
                    writer.write("\t");
                }

                writer.newLine();

                for (Pair<Map<String, String>, TObjectDoubleMap<String>> pair : values) {
                    Map<String, String> dimensions = pair.getLeft();
                    TObjectDoubleMap<String> hist = pair.getRight();
                    for (String key : predKeys) {
                        writer.write(dimensions.get(key));
                        writer.write("\t");
                    }

                    for(String key : dayKeys) {
                        writer.write(String.format(Locale.US, "%.4f", hist.get(key)));
                        writer.write("\t");
                    }

                    writer.newLine();
                }

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class EntityProvider implements ValueProvider<Integer, Attributable> {

        @Override
        public Integer get(Attributable attributable) {
            return 1;
        }
    }

    private static class DistancePredicate implements Predicate<Segment> {

        private final int index;

        private final Discretizer discretizer;

        public DistancePredicate(int index, Discretizer discretizer) {
            this.index = index;
            this.discretizer = discretizer;
        }

        @Override
        public boolean test(Segment segment) {
            String val = segment.getAttribute(CommonKeys.BEELINE_DISTANCE);
            if(val != null) {
                double d = Double.parseDouble(val);
                int idx = discretizer.index(d);
                return idx == index;
            }

            return false;
        }
    }
}
