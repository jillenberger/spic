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

import de.dbanalytics.spic.sim.data.CachedPerson;
import org.matsim.contrib.common.collections.Composite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class HamiltonianComposite extends Composite<Hamiltonian> implements Hamiltonian {

    private List<Double> thetas = new ArrayList<Double>();

    public void addComponent(Hamiltonian h) {
        super.addComponent(h);
        thetas.add(1.0);
    }

    public void addComponent(Hamiltonian h, double theta) {
        super.addComponent(h);
        thetas.add(theta);
    }

    public void removeComponent(Hamiltonian h) {
        int idx = components.indexOf(h);
        super.removeComponent(h);
        thetas.remove(idx);
    }

    /*
     * TODO: hide access?
     */
    public List<Hamiltonian> getComponents() {
        return components;
    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        double sum = 0;

        for (int i = 0; i < components.size(); i++) {
            sum += thetas.get(i) * components.get(i).evaluate(population);
        }

        return sum;
    }
}
