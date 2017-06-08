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
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;
import org.matsim.contrib.common.stats.StatsWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

import java.io.IOException;
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
            Discretizer refDiscretizer = createDiscretizer(mode);
            LegHistogramBuilder refHistogramBuilder = new LegAttributeHistogramBuilder(CommonKeys.LEG_GEO_DISTANCE, refDiscretizer);
            refHistogramBuilder.setPredicate(modePredicate);
            TDoubleDoubleMap refHistogram = refHistogramBuilder.build(engine.getRefPersons());

            TDoubleArrayList tmpBorders = createSimDiscretizer(mode);
            TDoubleDoubleMap simHist = HistogramTransformer.transform(tmpBorders, refHistogram);

            TDoubleArrayList borders = new TDoubleArrayList();
            borders.add(-1);
            borders.addAll(tmpBorders);
            Discretizer simDiscretizer = new FixedBordersDiscretizer(borders.toArray());
            LegHistogramBuilder simHistBuilder = new LegAttributeHistogramBuilder(CommonKeys.LEG_GEO_DISTANCE, simDiscretizer);
            simHistBuilder.setPredicate(modePredicate);

            try {
                FileIOContext ioContext = engine.getIOContext();
                String path = String.format("%s/%s.ref.%s.txt",
                        ioContext.getPath(),
                        ModeGeoDistanceBuilder.class.getSimpleName(),
                        mode);
                StatsWriter.writeHistogram((TDoubleDoubleHashMap) refHistogram, CommonKeys.LEG_GEO_DISTANCE, "count", path);
                path = String.format("%s/%s.sim.%s.txt",
                        ioContext.getPath(),
                        ModeGeoDistanceBuilder.class.getSimpleName(),
                        mode);
                StatsWriter.writeHistogram((TDoubleDoubleHashMap) simHist, CommonKeys.LEG_GEO_DISTANCE, "count", path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            UnivariatFrequency2 hamiltonian = new UnivariatFrequency2(
                    simHist,
                    simHistBuilder,
                    CommonKeys.LEG_GEO_DISTANCE,
                    simDiscretizer,
                    false,
                    false);

            hamiltonian.setPredicate(modePredicate);
            hamiltonian.setNoRefValError(2);
            hamiltonian.setErrorExponent(2.0);
//            hamiltonian.setResetInterval((long) 1e7);
            hamiltonian.setDebugMode(false);

            engine.getEngineListeners().addComponent(new DiscretDistributionDebugger(
                    hamiltonian,
                    "DebugModeDistance." + mode,
                    engine.getIOContext().getPath(),
                    (long) 1e7
            ));

            AnnealingHamiltonian annealingHamiltonian = AnnealingHamiltonianConfigurator.configure(
                    hamiltonian,
                    configGroup
            );

            engine.getHamiltonian().addComponent(annealingHamiltonian);
            engine.getEngineListeners().addComponent(annealingHamiltonian);

            engine.getAttributeListeners().get(CommonKeys.LEG_GEO_DISTANCE).addComponent(hamiltonian);

            HistogramWriter writer = new HistogramWriter(
                    engine.getIOContext(),
                    new PassThroughDiscretizerBuilder(simDiscretizer, "default"));
            AnalyzerTask<Collection<? extends Person>> analyzer = NumericLegAnalyzer.create(
                    CommonKeys.LEG_GEO_DISTANCE,
                    false,
                    modePredicate,
                    mode,
                    writer);
            engine.getHamiltonianAnalyzers().addComponent(analyzer);

            HistogramComparator comparator = new HistogramComparator(
                    simHist,
                    simHistBuilder,
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
            for (int d = 2000; d < 10000; d += 2000) borders.add(d);
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

    private static TDoubleArrayList createSimDiscretizer(String mode) {
        TDoubleArrayList borders = new TDoubleArrayList();
//        borders.add(-1);
        if (CommonValues.LEG_MODE_PED.equalsIgnoreCase(mode)) {
            for (int d = 500; d < 3000; d += 500) borders.add(d);
        } else if (CommonValues.LEG_MODE_BIKE.equalsIgnoreCase(mode)) {
            for (int d = 500; d < 5000; d += 500) borders.add(d);
            for (int d = 5000; d < 10000; d += 1000) borders.add(d);
            for (int d = 10000; d < 20000; d += 5000) borders.add(d);
        } else {
            for (int d = 2000; d < 20000; d += 2000) borders.add(d);
            for (int d = 20000; d < 50000; d += 5000) borders.add(d);
        }
        borders.add(Double.MAX_VALUE);
//        return new FixedBordersDiscretizer(borders.toArray());
        return borders;
    }
}
