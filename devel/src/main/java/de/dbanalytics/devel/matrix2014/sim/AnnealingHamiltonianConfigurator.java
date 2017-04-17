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

import de.dbanalytics.spic.sim.AnnealingHamiltonian;
import de.dbanalytics.spic.sim.Hamiltonian;
import org.matsim.core.config.ConfigGroup;

/**
 * @author johannes
 */
public class AnnealingHamiltonianConfigurator {

    public static AnnealingHamiltonian configure(Hamiltonian delegate, ConfigGroup configGroup) {
        AnnealingHamiltonian hamiltonian = new AnnealingHamiltonian(
                delegate,
                Double.parseDouble(configGroup.getValue("theta_min")),
                Double.parseDouble(configGroup.getValue("theta_max")));

        String value = configGroup.getValue("theta_factor");
        if(value != null) hamiltonian.setThetaFactor(Double.parseDouble(value));

        value = configGroup.getValue("delta_interval");
        if(value != null) hamiltonian.setDeltaInterval((long)Double.parseDouble(value));

        value = configGroup.getValue("delta_threshold");
        if(value != null) hamiltonian.setDeltaThreshold(Double.parseDouble(value));

        value = configGroup.getValue("startIteration");
        if(value != null) hamiltonian.setStartIteration((long)Double.parseDouble(value));

        return hamiltonian;
    }
}
