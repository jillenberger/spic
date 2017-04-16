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

import de.dbanalytics.spic.analysis.LegCollector;
import de.dbanalytics.spic.analysis.NumericAttributeProvider;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.ValueProvider;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import gnu.trove.map.TObjectDoubleMap;

import java.util.Collection;

/**
 * @author jillenberger
 */
public class LabeledLegHistogramBuilder {

    private LabeledHistogramBuilder builder;

    private LegCollector<String> valueCollector;

    private LegPersonCollector<Double> weightsCollector;

    public LabeledLegHistogramBuilder(ValueProvider<String, Segment> provider) {
        valueCollector = new LegCollector<>(provider);
        weightsCollector = new LegPersonCollector<>(new NumericAttributeProvider<Person>(CommonKeys.PERSON_WEIGHT));
        builder = new LabeledHistogramBuilder(valueCollector, weightsCollector);
    }

    public void setPredicate(Predicate<Segment> predicate) {
        valueCollector.setPredicate(predicate);
        weightsCollector.setPredicate(predicate);
    }

    public TObjectDoubleMap<String> build(Collection<? extends Person> persons) {
        return builder.build(persons);
    }
}
