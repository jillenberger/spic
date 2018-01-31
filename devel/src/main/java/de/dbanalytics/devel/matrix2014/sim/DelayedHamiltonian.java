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

package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.McmcSimulationObserver;
import de.dbanalytics.spic.sim.data.CachedPerson;

import java.util.Collection;

/**
 * @author johannes
 */
public class DelayedHamiltonian implements Hamiltonian, McmcSimulationObserver {

    private final long activationIteration;

    private final Hamiltonian hamiltonian;

    private long iteration = 0;

    public DelayedHamiltonian(Hamiltonian hamiltonian, long activationIteration) {
        this.hamiltonian = hamiltonian;
        this.activationIteration = activationIteration;
    }
    @Override
    public double evaluate(Collection<CachedPerson> population) {
        if(iteration >= activationIteration)
            return hamiltonian.evaluate(population);
        else
            return 0;
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        iteration++;
    }
}
