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

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.sim.AnnealingHamiltonian;
import de.dbanalytics.spic.sim.HamiltonianLogger;
import de.dbanalytics.devel.matrix2014.sim.UnivariatFrequency;
import de.dbanalytics.spic.sim.config.AnnealingHamiltonianConfigurator;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jillenberger
 */
public class GeoDistanceHamiltonian {

    public static final String MODULE_NAME = "geoDistanceHamiltonian";
    private static final Logger logger = Logger.getLogger(GeoDistanceHamiltonian.class);

    public static void build(Simulator engine, Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /*
        Get the elements the hamiltonian runs on.
         */
        Set<Attributable> refLegs = getCarLegs(engine.getRefPersons(), engine.getLegPredicate());
        Set<Attributable> simLegs = getCarLegs(engine.getSimPersons(), engine.getLegPredicate());
        logger.info(String.format("Initializing hamiltonian with %s ref legs and %s sim legs.", refLegs.size(),
                simLegs.size()));
        /*
        Create the geo distance discretizer.
         */
        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        for(int d = 2000; d < 10000; d += 2000) borders.add(d);
        for(int d = 10000; d < 50000; d += 10000) borders.add(d);
        for(int d = 50000; d < 500000; d += 50000) borders.add(d);
        for(int d = 500000; d < 1000000; d += 100000) borders.add(d);
        borders.add(Double.MAX_VALUE);
        Discretizer discretizer =  new FixedBordersDiscretizer(borders.toArray());
        /*
        Create and add the hamiltonian.
         */
        UnivariatFrequency hamiltonian = new UnivariatFrequency(
                refLegs,
                simLegs,
                CommonKeys.LEG_GEO_DISTANCE,
                discretizer,
                engine.getUseWeights());

//        double theta = Double.parseDouble(configGroup.getValue("theta"));
        AnnealingHamiltonian annealingHamiltonian = AnnealingHamiltonianConfigurator.configure(
                hamiltonian,
                configGroup);
        engine.getHamiltonian().addComponent(annealingHamiltonian);
        engine.getEngineListeners().addComponent(annealingHamiltonian);
        /*
        Add the hamiltonian to the geo distance attribute change listener.
         */
        engine.getAttributeListeners().get(CommonKeys.LEG_GEO_DISTANCE).addComponent(hamiltonian);
        /*
        Add a geo distance analyzer.
         */
        HistogramWriter writer = new HistogramWriter(
                engine.getIOContext(),
                new PassThroughDiscretizerBuilder(discretizer, "default"));
        AnalyzerTask<Collection<? extends Person>> analyzer = NumericLegAnalyzer.create(
                CommonKeys.LEG_GEO_DISTANCE,
                engine.getUseWeights(),
                engine.getLegPredicate(),
                engine.getLegPredicateName(),
                writer);
        engine.getHamiltonianAnalyzers().addComponent(analyzer);
        /*
        Add a hamiltonian logger.
         */
        engine.getEngineListeners().addComponent(new HamiltonianLogger(hamiltonian,
                engine.getLoggingInterval(),
                CommonKeys.LEG_GEO_DISTANCE,
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
