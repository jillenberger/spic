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
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class FactorHistogramBuilder {

    private Collector<String> valueCollector;

    private Collector<Double> weightsCollector;

    public FactorHistogramBuilder(Collector<String> valueCollector, Collector<Double> weightsCollector) {
        this.valueCollector = valueCollector;
        this.weightsCollector = weightsCollector;

    }

    public TObjectDoubleMap<String> build(Collection<? extends Person> persons) {
        List<String> values = valueCollector.collect(persons);
        List<Double> weights = weightsCollector.collect(persons);

        TObjectDoubleMap<String> hist = new TObjectDoubleHashMap<>();

        if(values.size() != weights.size()) throw new RuntimeException("Values and weights have to have equal length.");

        for(int i = 0; i < values.size(); i++) {
            hist.adjustOrPutValue(values.get(i), weights.get(i), weights.get(i));
        }

        return hist;
    }


}
