/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.MarkovEngineListener;
import de.dbanalytics.spic.sim.data.CachedPerson;

import java.util.Collection;

/**
 * @author johannes
 */
public class DelayedHamiltonian implements Hamiltonian, MarkovEngineListener {

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
