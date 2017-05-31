/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
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

package de.dbanalytics.spic.mid2008HH.processing;

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.SegmentTask;
import gnu.trove.map.TObjectDoubleMap;
import org.matsim.contrib.common.collections.ChoiceSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by johannesillenberger on 31.05.17.
 */
public class ImputeModes implements SegmentTask {

    private final ChoiceSet<String> choiceSet;

    public ImputeModes(Collection<? extends Person> persons, Random random) {
        List<Person> personList = new ArrayList<>(persons); //TODO: Should be moved to FactorHistogramBuilder
        Collector<String> valueCollector = new LegCollector<>(new AttributeProvider<>(CommonKeys.LEG_MODE));
        Collector<Double> weightsCollector = new LegPersonCollector(new NumericAttributeProvider<>(CommonKeys.PERSON_WEIGHT));
        FactorHistogramBuilder builder = new FactorHistogramBuilder(valueCollector, weightsCollector);
        TObjectDoubleMap<String> hist = builder.build(personList);

        choiceSet = new ChoiceSet<>(random);
        hist.forEachEntry((key, value) -> {
            if (key != null) choiceSet.addOption(key, value);
            return true;
        });

    }

    @Override
    public void apply(Segment segment) {
        String value = segment.getAttribute(CommonKeys.LEG_MODE);
        if (value == null) segment.setAttribute(CommonKeys.LEG_MODE, choiceSet.randomWeightedChoice());
    }
}
