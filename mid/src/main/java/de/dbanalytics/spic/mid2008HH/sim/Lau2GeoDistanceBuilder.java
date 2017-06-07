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
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.processing.CopyPersonAttToLeg;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.processing.TaskRunner;
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

/**
 * Created by johannesillenberger on 06.06.17.
 */
public class Lau2GeoDistanceBuilder {

    private static final String MODULE_NAME = "Lau2GeoDistance";

    private static final String LAU2_CLASS_AGGR = "lau2class_aggr";

    public static void build(Simulator engine, Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /**
         * Build aggregated Lau2 classes.
         */
        TaskRunner.run(new Lau2ClassAggregator(), engine.getRefPersons());
        TaskRunner.run(new Lau2ClassAggregator(), engine.getSimPersons());
        TaskRunner.run(new CopyPersonAttToLeg(LAU2_CLASS_AGGR), engine.getRefPersons());
        TaskRunner.run(new CopyPersonAttToLeg(LAU2_CLASS_AGGR), engine.getSimPersons());
        /**
         *
         */
        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        for (int d = 2000; d < 10000; d += 2000) borders.add(d);
        for (int d = 10000; d < 22000; d += 4000) borders.add(d);
        borders.add(Double.MAX_VALUE);
        Discretizer discretizer = new FixedBordersDiscretizer(borders.toArray());

        String[] klasses = new String[]{"1", "2", "3"};
        for (String klass : klasses) {
            Predicate<Segment> klassPredicate = new LegAttributePredicate(LAU2_CLASS_AGGR, klass);
            LegHistogramBuilder histogramBuilder = new LegAttributeHistogramBuilder(CommonKeys.LEG_GEO_DISTANCE, discretizer);
            histogramBuilder.setPredicate(klassPredicate);
            TDoubleDoubleMap historgram = histogramBuilder.build(engine.getRefPersons());

            UnivariatFrequency2 hamiltonian = new UnivariatFrequency2(
                    historgram,
                    histogramBuilder,
                    CommonKeys.LEG_GEO_DISTANCE,
                    discretizer,
                    false,
                    false);

            hamiltonian.setPredicate(klassPredicate);
            hamiltonian.setNoRefValError(2);
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
                    klassPredicate,
                    klass,
                    writer);
            engine.getHamiltonianAnalyzers().addComponent(analyzer);

            HistogramComparator comparator = new HistogramComparator(
                    historgram,
                    histogramBuilder,
                    String.format("%s.%s", CommonKeys.LEG_GEO_DISTANCE, klass));
            comparator.setFileIoContext(engine.getIOContext());
            engine.getHamiltonianAnalyzers().addComponent(comparator);
        /*
        Add a hamiltonian logger.
         */
            engine.getEngineListeners().addComponent(new HamiltonianLogger(hamiltonian,
                    engine.getLoggingInterval(),
                    String.format("%s.%s", CommonKeys.LEG_GEO_DISTANCE, klass),
                    engine.getIOContext().getRoot(),
                    annealingHamiltonian.getStartIteration()));
        }

    }

    private static final class Lau2ClassAggregator implements PersonTask {

        @Override
        public void apply(Person person) {
            String value = person.getAttribute(MiDKeys.PERSON_LAU2_CLASS);
            if (value != null) {
                int idx = Integer.parseInt(value);
                if (idx == 7) person.setAttribute(LAU2_CLASS_AGGR, "3");
                else if (idx >= 3) person.setAttribute(LAU2_CLASS_AGGR, "2");
                else if (idx >= 1) person.setAttribute(LAU2_CLASS_AGGR, "1");
            }
        }
    }
}