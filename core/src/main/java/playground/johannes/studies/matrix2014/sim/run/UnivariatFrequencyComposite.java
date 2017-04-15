/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package playground.johannes.studies.matrix2014.sim.run;

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
