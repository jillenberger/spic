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

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.processing.EpisodeTask;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import org.matsim.contrib.common.collections.ChoiceSet;

import java.util.Collection;
import java.util.Random;

/**
 * Created by johannesillenberger on 10.05.17.
 */
public class GuessMissingPurposes implements EpisodeTask {

    private ChoiceSet<String> shortChoiceSet;

    private ChoiceSet<String> longChoiceSet;

    private Predicate<Segment> distancePredicate;

    public GuessMissingPurposes(Collection<? extends Person> refPersons, Predicate<Segment> predicate, Random random) {
        FactorLegHistogramBuilder builder = new FactorLegHistogramBuilder(new AttributeProvider<Segment>(Attributes.KEY.TRAVEL_PURPOSE));
        /*
        short distances
         */
        distancePredicate = new ShortDistancePredicate();
        builder.setPredicate(PredicateAndComposite.create(predicate, distancePredicate));
        TObjectDoubleMap<String> hist = builder.build(refPersons);

        shortChoiceSet = new ChoiceSet<>(random);
        TObjectDoubleIterator<String> it = hist.iterator();
        for (int i = 0; i < hist.size(); i++) {
            it.advance();
            shortChoiceSet.addOption(it.key(), it.value());
        }
        /*
        long distances
         */
        builder.setPredicate(PredicateAndComposite.create(predicate, new NotPredicate<>(distancePredicate)));
        hist = builder.build(refPersons);

        longChoiceSet = new ChoiceSet<>(random);
        it = hist.iterator();
        for (int i = 0; i < hist.size(); i++) {
            it.advance();
            longChoiceSet.addOption(it.key(), it.value());
        }
    }

    @Override
    public void apply(Episode episode) {
        for (Segment leg : episode.getLegs()) {
            if (leg.getAttribute(Attributes.KEY.TRAVEL_PURPOSE) == null) {
                String purpose;
                if (distancePredicate.test(leg))
                    purpose = shortChoiceSet.randomWeightedChoice();
                else
                    purpose = longChoiceSet.randomWeightedChoice();

                leg.setAttribute(Attributes.KEY.TRAVEL_PURPOSE, purpose);
            }
        }
    }

    public static class ShortDistancePredicate implements Predicate<Segment> {

        @Override
        public boolean test(Segment segment) {
            String val = segment.getAttribute(Attributes.KEY.BEELINE_DISTANCE);
            if (val != null) {
                double dist = Double.parseDouble(val);
                if (dist < 100000) return true;
                else return false;
            }

            return true;
        }
    }
}
