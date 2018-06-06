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

package de.dbanalytics.devel.matrix2014.demo;

import de.dbanalytics.spic.mid2008.MidAttributes;
import de.dbanalytics.spic.sim.*;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;

import java.util.Random;

/**
 * @author johannes
 */
public class AgeMutatorBuilder implements MutatorBuilder<CachedPerson> {

    private final Object ageDataKey;

    private final Random random;

    private final RandomIntGenerator generator;

    private final AttributeObserver listener;

    public AgeMutatorBuilder(AttributeObserver listener, Random random) {
        this.listener = listener;
        this.random = random;
        generator = new RandomIntGenerator(random, 0, 100);

        ageDataKey = Converters.register(MidAttributes.KEY.AGE, DoubleConverter.getInstance());
    }

    @Override
    public Mutator<CachedPerson> build() {
        RandomElementMutator em = new AttributeMutator(ageDataKey, generator, listener);
        Mutator<CachedPerson> m = new RandomPersonMutator(em, random);
        return m;
    }
}
