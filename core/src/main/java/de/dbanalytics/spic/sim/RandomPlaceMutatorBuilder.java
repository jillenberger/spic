/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.NotPredicate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.sim.data.CachedSegment;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.PlaceConverter;

import java.util.Random;
import java.util.Set;

/**
 * Created by johannesillenberger on 07.06.17.
 */
public class RandomPlaceMutatorBuilder implements MutatorBuilder<CachedSegment> {

    private Set<Place> places;

    private AttributeChangeListener listener;

    private Random random;

    public RandomPlaceMutatorBuilder(Set<Place> places, AttributeChangeListener listener, Random random) {
        this.places = places;
        this.listener = listener;
        this.random = random;
    }

    @Override
    public Mutator<CachedSegment> build() {
        PlaceIndex index = new PlaceIndex(places);
        Object dataKey = Converters.register(CommonKeys.ACTIVITY_FACILITY, new PlaceConverter(index));

        RandomPlaceGenerator generator = new RandomPlaceGenerator(places, random);
        generator.setPredicate(new NotPredicate<>(new HomePredicate()));

        AttributeMutator attMutator = new AttributeMutator(dataKey, generator, listener);
        RandomActMutator actMutator = new RandomActMutator(attMutator, random);

        return actMutator;
    }
}
