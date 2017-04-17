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

package de.dbanalytics.devel.matrix2014.sim.run;

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.AttributeChangeListener;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedPerson;

import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class UnivariatFrequencyComposite<A extends Attributable> implements Hamiltonian, AttributeChangeListener {

    public UnivariatFrequencyComposite(List<Predicate<A>> predicates) {
        for(Predicate<A> predicate : predicates) {

        }
    }

    @Override
    public void onChange(Object dataKey, Object oldValue, Object newValue, CachedElement element) {

    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        return 0;
    }
}
