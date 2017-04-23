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

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.LinearDiscretizer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class LegPurposeDistanceTask implements AnalyzerTask<Collection<? extends Person>> {

    private Predicate<Segment> legPredicate;

    private Discretizer discretizer;

    private FileIOContext ioContext;

    private static final String SEPARATOR = "\t";

    public LegPurposeDistanceTask() {
        discretizer = new LinearDiscretizer(50000);
    }

    public void setPredicate(Predicate<Segment> predicate) {
        this.legPredicate = predicate;
    }

    public void setDiscretizer(Discretizer discretizer) {
        this.discretizer = discretizer;
    }

    public void setIoContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        Collector<String> collector = new LegCollector<>(new AttributeProvider<Segment>(CommonKeys.LEG_PURPOSE));
        Set<String> types = new HashSet<>(collector.collect(persons));
        types.remove(null);

        LegAttributeHistogramBuilder builder = new LegAttributeHistogramBuilder(CommonKeys.LEG_GEO_DISTANCE, discretizer);

        Map<String, TDoubleDoubleMap> histograms = new HashMap<>();
        for(String type : types) {
            Predicate<Segment> predicate = new LegAttributePredicate(CommonKeys.LEG_PURPOSE, type);
            if(legPredicate != null) predicate = PredicateAndComposite.create(predicate, legPredicate);
            builder.setPredicate(predicate);

            TDoubleDoubleMap hist = builder.build(persons);
            histograms.put(type, hist);
        }

        if(ioContext != null) {
            try {
                SortedSet<Double> keySet = new TreeSet<>();
                for(TDoubleDoubleMap hist : histograms.values()) {
                    double keys[] = hist.keys();
                    for(int i = 0; i < keys.length; i++) {
                        keySet.add(keys[i]);
                    }
                }

                writeHistograms(histograms, keySet, String.format("%s/purposeGeoDistance.txt", ioContext.getPath()));

                for(double key : keySet) {
                    double sum = 0;
                    for(TDoubleDoubleMap h : histograms.values()) {
                        sum += h.get(key);
                    }

                    for(TDoubleDoubleMap h : histograms.values()) {
                        h.put(key, h.get(key) / sum);
                    }
                }

                writeHistograms(histograms, keySet, String.format("%s/purposeGeoDistance.norm.txt", ioContext.getPath()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeHistograms(Map<String, TDoubleDoubleMap> histograms, Set<Double> keys, String file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.write("purpose");

        for(double key : keys) {
            writer.write(SEPARATOR);
            writer.write(String.valueOf(key));
        }

        writer.newLine();

        for(Map.Entry<String, TDoubleDoubleMap> entry : histograms.entrySet()) {
            String type = entry.getKey();
            TDoubleDoubleMap hist = entry.getValue();

            writer.write(type);

            for(double key : keys) {
                writer.write(SEPARATOR);
                writer.write(String.valueOf(hist.get(key)));
            }

            writer.newLine();
        }

        writer.close();
    }
}