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

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.sim.HistogramBuilder;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;

import java.util.Collection;

/**
 * @author jillenberger
 */
public class LegHistogramBuilder implements HistogramBuilder {

    private DefaultHistogramBuilder builder;

    private LegCollector<Double> valueCollector;

    private LegPersonCollector<Double> weightsCollector;

    public LegHistogramBuilder(ValueProvider<Double, Segment> provider, Discretizer discretizer) {
        this(provider, discretizer, true); //TODO: Default value is true for compatibility but should be false by design.
    }

    public LegHistogramBuilder(ValueProvider<Double, Segment> provider, Discretizer discretizer, boolean useWeights) {
        valueCollector = new LegCollector<>(provider);
        if (useWeights) {
            weightsCollector = new LegPersonCollector<>(new NumericAttributeProvider<>(CommonKeys.PERSON_WEIGHT));
        } else {
            weightsCollector = new LegPersonCollector<>(new DefaultWeightProvider());
        }

        builder = new DefaultHistogramBuilder(valueCollector, weightsCollector, discretizer);
    }

    public void setPredicate(Predicate<Segment> predicate) {
        valueCollector.setPredicate(predicate);
        weightsCollector.setPredicate(predicate);
    }

    @Override
    public TDoubleDoubleMap build(Collection<? extends Person> persons) {
        return builder.build(persons);
    }

    private class DefaultWeightProvider implements ValueProvider<Double, Person> {

        @Override
        public Double get(Person attributable) {
            return 1.0;
        }
    }
}
