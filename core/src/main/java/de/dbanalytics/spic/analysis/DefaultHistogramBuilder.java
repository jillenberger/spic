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
import de.dbanalytics.spic.sim.HistogramBuilder;
import de.dbanalytics.spic.util.CollectionUtils;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;

import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class DefaultHistogramBuilder implements HistogramBuilder {

    private Collector<Double> valueCollector;

    private Collector<Double> weightsCollector;

    private Discretizer discretizer;

    private boolean reweight;

    public DefaultHistogramBuilder(Collector<Double> valueCollector, Collector<Double> weightsCollector, Discretizer discretizer) {
        this.valueCollector = valueCollector;
        this.weightsCollector = weightsCollector;
        this.discretizer = discretizer;
        setReweight(false);
    }

    public void setReweight(boolean reweight) {
        this.reweight = reweight;
    }

    public TDoubleDoubleMap build(Collection<? extends Person> persons) {
        List<Double> values = valueCollector.collect(persons);
        List<Double> weights = weightsCollector.collect(persons);
        List<double[]> nativeValues = CollectionUtils.toNativeArray(values, weights);
        TDoubleDoubleMap hist = Histogram.createHistogram(nativeValues.get(0), nativeValues.get(1), discretizer, reweight);
        return hist;
    }


}
