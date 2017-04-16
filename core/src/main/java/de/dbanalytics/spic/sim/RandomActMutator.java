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

import de.dbanalytics.spic.sim.data.CachedEpisode;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.CachedSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jillenberger
 */
public class RandomActMutator implements Mutator<CachedSegment> {

    private final RandomElementMutator delegate;

    private final Random random;

    private final List<CachedSegment> mutation;

    public RandomActMutator(RandomElementMutator delegate, Random random) {
        this.delegate = delegate;
        this.random = random;

        mutation = new ArrayList<>(1);
        mutation.add(null);
    }

    @Override
    public List<CachedSegment> select(List<CachedPerson> population) {
        CachedPerson p = population.get(random.nextInt(population.size()));
        CachedEpisode e = (CachedEpisode) p.getEpisodes().get(0); //TODO: Or better random episode?
        CachedSegment s = (CachedSegment) e.getActivities().get(random.nextInt(e.getActivities().size()));

        mutation.set(0, s);

        return mutation;
    }

    @Override
    public boolean modify(List<CachedSegment> elements) {
        return delegate.modify(mutation.get(0));
    }

    @Override
    public void revert(List<CachedSegment> elements) {
        delegate.revert(mutation.get(0));
    }
}
