/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.mid2008HH.sim;

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.sim.AnnealingHamiltonian;
import de.dbanalytics.spic.sim.HamiltonianLogger;
import de.dbanalytics.spic.sim.UnivariatFrequency2;
import de.dbanalytics.spic.sim.config.AnnealingHamiltonianConfigurator;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by johannesillenberger on 30.05.17.
 */
public class ModeGeoDistanceBuilder {

    private static final String MODULE_NAME = "ModeGeoDistance";

    public static void build(Simulator engine, Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /**
         * Get all modes
         */
        Set<String> modes = new HashSet<>();
        for (Person person : engine.getRefPersons()) {
            for (Episode episode : person.getEpisodes()) {
                for (Segment leg : episode.getLegs()) {
                    String mode = leg.getAttribute(CommonKeys.LEG_MODE);
                    if (mode != null) modes.add(mode);
                }
            }
        }
        /**
         * For each mode...
         */
        for (String mode : modes) {
            Predicate<Segment> modePredicate = new LegAttributePredicate(CommonKeys.LEG_MODE, mode);
//            Discretizer discretizer = createDiscretizer(modePredicate, engine.getRefPersons());
            Discretizer discretizer = createDiscretizer(mode);
            LegHistogramBuilder histogramBuilder = new LegAttributeHistogramBuilder(CommonKeys.LEG_GEO_DISTANCE, discretizer);
            histogramBuilder.setPredicate(modePredicate);

            TDoubleDoubleMap histogram = histogramBuilder.build(engine.getRefPersons());

            UnivariatFrequency2 hamiltonian = new UnivariatFrequency2(
                    histogram,
                    histogramBuilder,
                    CommonKeys.LEG_GEO_DISTANCE,
                    discretizer,
                    false,
                    false);

            hamiltonian.setErrorExponent(2.0);
            hamiltonian.setResetInterval((long) 5e8);

            AnnealingHamiltonian annealingHamiltonian = AnnealingHamiltonianConfigurator.configure(
                    hamiltonian,
                    configGroup
            );

            engine.getHamiltonian().addComponent(annealingHamiltonian);
            engine.getEngineListeners().addComponent(annealingHamiltonian);

            engine.getAttributeListeners().get(CommonKeys.LEG_GEO_DISTANCE).addComponent(hamiltonian);

            HistogramWriter writer = new HistogramWriter(
                    engine.getIOContext(),
                    new PassThroughDiscretizerBuilder(discretizer, "default"));
            AnalyzerTask<Collection<? extends Person>> analyzer = NumericLegAnalyzer.create(
                    CommonKeys.LEG_GEO_DISTANCE,
                    false,
                    modePredicate,
                    mode,
                    writer);
            engine.getHamiltonianAnalyzers().addComponent(analyzer);

            HistogramComparator comparator = new HistogramComparator(
                    histogram,
                    histogramBuilder,
                    String.format("%s.%s", CommonKeys.LEG_GEO_DISTANCE, mode));
            comparator.setFileIoContext(engine.getIOContext());
            engine.getHamiltonianAnalyzers().addComponent(comparator);
        /*
        Add a hamiltonian logger.
         */
            engine.getEngineListeners().addComponent(new HamiltonianLogger(hamiltonian,
                    engine.getLoggingInterval(),
                    String.format("%s.%s", CommonKeys.LEG_GEO_DISTANCE, mode),
                    engine.getIOContext().getRoot(),
                    annealingHamiltonian.getStartIteration()));
        }
    }

//    private static Discretizer createDiscretizer(Predicate<Segment> modePredicate, Collection<? extends Person> persons) {
//        ValueProvider<Double, Segment> provider = new NumericAttributeProvider<>(CommonKeys.LEG_GEO_DISTANCE);
//        LegCollector<Double> collector = new LegCollector<>(provider);
//        collector.setPredicate(modePredicate);
//        double[] values = CollectionUtils.toNativeArray(collector.collect(persons));
//        return FixedSampleSizeDiscretizer.create(values, 200);
//    }

    private static Discretizer createDiscretizer(String mode) {
        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        if (CommonValues.LEG_MODE_CAR.equalsIgnoreCase(mode)) {
            for (int d = 1000; d < 10000; d += 1000) borders.add(d);
            for (int d = 10000; d < 50000; d += 5000) borders.add(d);
        } else if (CommonValues.LEG_MODE_RIDE.equalsIgnoreCase(mode) ||
                CommonValues.LEG_MODE_PT.equalsIgnoreCase(mode)) {
            for (int d = 2000; d < 10000; d += 2000) borders.add(d);
            for (int d = 10000; d < 50000; d += 10000) borders.add(d);
        } else if (CommonValues.LEG_MODE_PED.equalsIgnoreCase(mode) ||
                CommonValues.LEG_MODE_BIKE.equalsIgnoreCase(mode)) {
            for (int d = 500; d < 3000; d += 500) borders.add(d);
            for (int d = 3000; d < 9000; d += 3000) borders.add(d);
        }
        borders.add(Double.MAX_VALUE);
        return new FixedBordersDiscretizer(borders.toArray());
    }
}
