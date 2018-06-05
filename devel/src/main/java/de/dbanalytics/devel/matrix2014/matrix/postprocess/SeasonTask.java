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

package de.dbanalytics.devel.matrix2014.matrix.postprocess;

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
public class SeasonTask implements AnalyzerTask<Collection<? extends Person>> {

    private FileIOContext ioContext;

    public SeasonTask(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        /*
        Get all purposes.
         */
        LegCollector<String> purposeCollector = new LegCollector<>(new AttributeProvider<>(Attributes.KEY.TRAVEL_PURPOSE));
        Set<String> purposes = new HashSet<>(purposeCollector.collect(persons));
        purposes.remove(null);
        /*
        Get all seasons
         */
        PersonCollector<String> seasonCollector = new PersonCollector<>(new AttributeProvider<>(SetSeason.SEASON_KEY));
        Set<String> seasons = new HashSet<>(seasonCollector.collect(persons));
        seasons.remove(null);

        List<Pair<Map<String, String>, TObjectDoubleMap<String>>> shareHists = new ArrayList<>();
        List<Pair<Map<String, String>, TObjectDoubleMap<String>>> absoluteHists = new ArrayList<>();

        Predicate<Segment> modePredicate = new LegAttributePredicate(Attributes.KEY.MODE, Attributes.MODE.CAR);

        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        borders.add(50000);
        borders.add(100000);
        borders.add(1000000);
        borders.add(Double.MAX_VALUE);
        Discretizer discretizer = new FixedBordersDiscretizer(borders.toArray());

        for (String purpose : purposes) {
            Predicate<Segment> purposePredicate = new LegAttributePredicate(Attributes.KEY.TRAVEL_PURPOSE, purpose);

            for(int idx = 1; idx < borders.size() - 1; idx ++) {
                Predicate<Segment> distPrediate = new DistancePredicate(idx, discretizer);

                TObjectDoubleMap<String> hist = new TObjectDoubleHashMap<>();
                TObjectDoubleMap<String> histShare = new TObjectDoubleHashMap<>();
                for (String season : seasons) {
                    Predicate<Segment> seasonPredicate = new LegPersonAttributePredicate(SetSeason.SEASON_KEY, season);

                    Predicate<Segment> predicate = PredicateAndComposite.create(modePredicate,
                            purposePredicate,
                            distPrediate,
                            seasonPredicate);

                    LegPersonCollector<Double> counter = new LegPersonCollector(new NumericAttributeProvider<>(Attributes.KEY.WEIGHT));
                    counter.setPredicate(predicate);
                    List<Double> weights = counter.collect(persons);
                    hist.put(season, sum(weights));
                    histShare.put(season, sum(weights));
                }

                Map<String, String> dimensions = new HashMap<>();
                dimensions.put(Attributes.KEY.MODE, Attributes.MODE.CAR);
                dimensions.put(Attributes.KEY.TRAVEL_PURPOSE, purpose);
                dimensions.put(Attributes.KEY.BEELINE_DISTANCE, String.valueOf(borders.get(idx)));

                absoluteHists.add(new ImmutablePair<>(dimensions, hist));
                Histogram.normalize(histShare);
                shareHists.add(new ImmutablePair<>(dimensions, histShare));
            }
        }

        if(ioContext != null) {
            write(absoluteHists, "season.txt");
            write(shareHists, "season.share.txt");
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

                Set<String> keys = values.get(0).getLeft().keySet();
                for (String key : keys) {
                    writer.write(key);
                    writer.write("\t");
                }
                writer.write(SetSeason.SUMMER);
                writer.write("\t");
                writer.write(SetSeason.WINTER);
                writer.newLine();

                for (Pair<Map<String, String>, TObjectDoubleMap<String>> pair : values) {
                    Map<String, String> dimensions = pair.getLeft();
                    TObjectDoubleMap<String> hist = pair.getRight();
                    for (String key : keys) {
                        writer.write(dimensions.get(key));
                        writer.write("\t");
                    }

                    writer.write(String.format(Locale.US, "%.4f",hist.get(SetSeason.SUMMER)));
                    writer.write("\t");
                    writer.write(String.format(Locale.US, "%.4f", hist.get(SetSeason.WINTER)));
                    writer.newLine();
                }

                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private static class EntityProvider implements ValueProvider<Integer, Attributable> {
//
//        @Override
//        public Integer get(Attributable attributable) {
//            return 1;
//        }
//    }

    private static class DistancePredicate implements Predicate<Segment> {

        private final int index;

        private final Discretizer discretizer;

        public DistancePredicate(int index, Discretizer discretizer) {
            this.index = index;
            this.discretizer = discretizer;
        }

        @Override
        public boolean test(Segment segment) {
            String val = segment.getAttribute(Attributes.KEY.BEELINE_DISTANCE);
            if(val != null) {
                double d = Double.parseDouble(val);
                int idx = discretizer.index(d);
                return idx == index;
            }

            return false;
        }
    }
}
