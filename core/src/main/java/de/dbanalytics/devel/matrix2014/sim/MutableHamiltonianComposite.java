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

import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.matsim.contrib.common.collections.Composite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class MutableHamiltonianComposite extends Composite<Hamiltonian> implements Hamiltonian {

    private List<ThetaProvider> providers = new ArrayList<>();

    public void addComponent(Hamiltonian h) {
        super.addComponent(h);
        providers.add(null);
    }

    public void addComponent(Hamiltonian h, ThetaProvider provider) {
        super.addComponent(h);
        providers.add(provider);
    }

    public void removeComponent(Hamiltonian h) {
        int idx = components.indexOf(h);
        super.removeComponent(h);
        providers.remove(idx);
    }

//    /*
//     * TODO: hide access?
//     */
//    public List<Hamiltonian> getComponents() {
//        return components;
//    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        double sum = 0;

        for (int i = 0; i < components.size(); i++) {
            sum += providers.get(i).getTheta() * components.get(i).evaluate(population);
        }

        return sum;
    }
}
