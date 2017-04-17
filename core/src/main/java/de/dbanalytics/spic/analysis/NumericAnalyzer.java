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
package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.data.Person;
import org.matsim.contrib.common.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class NumericAnalyzer implements AnalyzerTask<Collection<? extends Person>> {

    private final Collector<Double> collector;

    private final Collector<Double> weightsCollector;

    private final String dimension;

    private final HistogramWriter histogramWriter;

    public NumericAnalyzer(Collector<Double> collector, String dimension, HistogramWriter histogramWriter) {
        this(collector, null, dimension, histogramWriter);
    }

    public NumericAnalyzer(Collector<Double> collector, Collector<Double> weightsCollector, String dimension, HistogramWriter histogramWriter) {
        this.collector = collector;
        this.weightsCollector = weightsCollector;
        this.dimension = dimension;
        this.histogramWriter = histogramWriter;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        if (weightsCollector == null) {
            List<Double> values = collector.collect(persons);
            containers.add(new StatsContainer(dimension, values));

            if (histogramWriter != null) {
                double[] doubleValues = CollectionUtils.toNativeArray(values);
                histogramWriter.writeHistograms(doubleValues, dimension);
            }
        } else {
            Collection<? extends Person> personsList = persons;
            if (!(persons instanceof List)) {
                personsList = new ArrayList<>(persons);
            }

            List<Double> values = collector.collect(personsList);
            List<Double> weights = weightsCollector.collect(personsList);

            containers.add(new StatsContainer(dimension, values, weights));

            if (histogramWriter != null) {
                List<double[]> valuesList = de.dbanalytics.spic.util.CollectionUtils.toNativeArray(values, weights);
                histogramWriter.writeHistograms(valuesList.get(0), valuesList.get(1), dimension);
            }
        }

    }
}
