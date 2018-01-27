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
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.processing.CopyPersonAttToLeg;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.sim.AnnealingHamiltonian;
import de.dbanalytics.devel.matrix2014.sim.BivariatMean;
import de.dbanalytics.spic.sim.HamiltonianLogger;
import de.dbanalytics.spic.sim.config.AnnealingHamiltonianConfigurator;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

import java.util.HashSet;
import java.util.Set;

/**
 * @author jillenberger
 */
public class MeanDistanceHamiltonian {

    public static final String MODULE_NAME = "meanDistanceHamiltonian";

    public static void build(Simulator engine, Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /*
        Copy the lau2 class attribute from the person element to the corresponding leg elements.
         */
        TaskRunner.run(new CopyPersonAttToLeg(MiDKeys.PERSON_LAU2_CLASS), engine.getRefPersons());
        TaskRunner.run(new CopyPersonAttToLeg(MiDKeys.PERSON_LAU2_CLASS), engine.getSimPersons());
        /*
        Get the legs.
         */
        Set<Attributable> refLegs = getCarLegs(engine.getRefPersons(), engine.getLegPredicate());
        Set<Attributable> simLegs = getCarLegs(engine.getSimPersons(), engine.getLegPredicate());
        /*
        Build and add the hamiltonian.
         */
        Converters.register(MiDKeys.PERSON_LAU2_CLASS, DoubleConverter.getInstance());

        BivariatMean hamiltonian = new BivariatMean(
                refLegs,
                simLegs,
                MiDKeys.PERSON_LAU2_CLASS,
                CommonKeys.LEG_GEO_DISTANCE,
                new LinearDiscretizer(1.0),
                engine.getUseWeights());

        AnnealingHamiltonian annealingHamiltonian = AnnealingHamiltonianConfigurator.configure(
                hamiltonian,
                configGroup);
        engine.getHamiltonian().addComponent(annealingHamiltonian);
        engine.getEngineListeners().addComponent(annealingHamiltonian);
        engine.getAttributeListeners().get(CommonKeys.LEG_GEO_DISTANCE).addComponent(hamiltonian);
        /*
        Add a hamiltonian logger.
         */
        engine.getEngineListeners().addComponent(new HamiltonianLogger(hamiltonian,
                engine.getLoggingInterval(),
                "meanDistanceLAU2",
                engine.getIOContext().getRoot()));
    }

    private static Set<Attributable> getCarLegs(Set<? extends Person> persons, Predicate<Segment> predicate) {
        Set<Attributable> legs = new HashSet<>();
        for (Person p : persons) {
            Episode e = p.getEpisodes().get(0);
            for (Segment leg : e.getLegs()) {
                if (predicate.test(leg)) legs.add(leg);
            }
        }

        return legs;
    }
}
