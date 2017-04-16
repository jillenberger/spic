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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.matsim.contrib.common.collections.ChoiceSet;

import java.util.List;
import java.util.Random;

/**
 * @author johannes
 */
public class MutatorComposite<T extends Attributable> implements Mutator<T> {

    private final ChoiceSet<Mutator> mutators;

    private Mutator active;

    public MutatorComposite(Random random) {
        mutators = new ChoiceSet<>(random);
    }

    public void addMutator(Mutator mutator) {
        mutators.addOption(mutator);
    }

    @Override
    public List<T> select(List<CachedPerson> persons) {
        active = mutators.randomChoice();
        return active.select(persons);
    }

    @Override
    public boolean modify(List<T> persons) {
        return active.modify(persons);
    }

    @Override
    public void revert(List<T> persons) {
        active.revert(persons);

    }

}
