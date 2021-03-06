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

package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.sim.HistogramBuilder;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;

import java.util.Collection;

/**
 * Created by johannesillenberger on 11.05.17.
 */
public class ActHistogramBuilder implements HistogramBuilder {

    private DefaultHistogramBuilder builder;

    private ActCollector<Double> valueCollector;

    private ActPersonCollector<Double> weightsCollector;

    public ActHistogramBuilder(ValueProvider<Double, Segment> provider, Discretizer discretizer) {
        valueCollector = new ActCollector<>(provider);
        weightsCollector = new ActPersonCollector<>(new NumericAttributeProvider<Person>(Attributes.KEY.WEIGHT));
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
}
