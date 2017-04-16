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

import de.dbanalytics.devel.matrix2014.config.ODCalibratorConfigurator;
import de.dbanalytics.devel.matrix2014.sim.AnnealingHamiltonian;
import de.dbanalytics.devel.matrix2014.sim.AnnealingHamiltonianConfigurator;
import de.dbanalytics.devel.matrix2014.sim.CachedModePredicate;
import de.dbanalytics.devel.matrix2014.sim.ODCalibrator;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.sim.HamiltonianLogger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

/**
 * @author jillenberger
 */
public class ODCalibratorHamiltonian {

    private static final String MODULE_NAME = "odCalibratorHamiltonian";

    public static void build(Simulator engine, Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        ODCalibrator hamiltonian = new ODCalibratorConfigurator(
                engine.getDataPool())
                .configure(configGroup);

        hamiltonian.setUseWeights(engine.getUseWeights());
        hamiltonian.setPredicate(new CachedModePredicate(CommonKeys.LEG_MODE, CommonValues.LEG_MODE_CAR));

        AnnealingHamiltonian annealingHamiltonian = AnnealingHamiltonianConfigurator.configure(hamiltonian,
                configGroup);
        engine.getHamiltonian().addComponent(annealingHamiltonian);
        engine.getAttributeListeners().get(CommonKeys.ACTIVITY_FACILITY).addComponent(hamiltonian);
        engine.getEngineListeners().addComponent(annealingHamiltonian);
        /*
        Add a hamiltonian logger.
         */
        long start = 0;
        String value = configGroup.getValue("startIteration");
        if(value != null) start = (long)Double.parseDouble(value);

        engine.getEngineListeners().addComponent(new HamiltonianLogger(hamiltonian,
                engine.getLoggingInterval(),
                "odCalibrator",
                engine.getIOContext().getRoot(),
                start));
    }
}
