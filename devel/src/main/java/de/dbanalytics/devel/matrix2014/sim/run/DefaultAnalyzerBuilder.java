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

import de.dbanalytics.devel.matrix2014.analysis.ActTypeDistanceTask;
import de.dbanalytics.devel.matrix2014.analysis.LegPurposeDistanceTask;
import de.dbanalytics.devel.matrix2014.analysis.SetSeason;
import de.dbanalytics.devel.matrix2014.analysis.ZoneMobilityRate;
import de.dbanalytics.devel.matrix2014.matrix.postprocess.SeasonTask;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.devel.matrix2014.gis.ZoneCollection;
import de.dbanalytics.devel.matrix2014.gis.ZoneData;
import de.dbanalytics.devel.matrix2014.gis.ZoneDataLoader;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.mid2008.MiDValues;
import de.dbanalytics.spic.processing.TaskRunner;
import gnu.trove.list.array.TDoubleArrayList;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedBordersDiscretizer;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.core.config.Config;

import java.util.Collection;

/**
 * @author jillenberger
 */
public class DefaultAnalyzerBuilder {

    public static void build(Simulator engine, Config config) {
        AnalyzerTaskComposite<Collection<? extends Person>> task = engine.getAnalyzerTasks();
        /*
        geo distance
         */
        ConcurrentAnalyzerTask<Collection<? extends Person>> composite = new ConcurrentAnalyzerTask<>();

        HistogramWriter histogramWriter = new HistogramWriter(engine.getIOContext(), new StratifiedDiscretizerBuilder(100, 100));
        histogramWriter.addBuilder(new PassThroughDiscretizerBuilder(new LinearDiscretizer(50000), "linear"));
        histogramWriter.addBuilder(new PassThroughDiscretizerBuilder(new LinearDiscretizer(25000), "linear25"));
        histogramWriter.addBuilder(new PassThroughDiscretizerBuilder(new FixedBordersDiscretizer(new double[]{-1,
                100000, Integer.MAX_VALUE}), "100KM"));

        composite.addComponent(NumericLegAnalyzer.create(
                Attributes.KEY.TRIP_DISTANCE,
                engine.getUseWeights(),
                engine.getLegPredicate(),
                engine.getLegPredicateName(),
                histogramWriter));

        composite.addComponent(NumericLegAnalyzer.create(
                Attributes.KEY.BEELINE_DISTANCE, engine.getUseWeights(),
                engine.getLegPredicate(),
                engine.getLegPredicateName(),
                histogramWriter));

        for (int klass = 0; klass < 6; klass++) {
            Predicate<Segment> lauPred = new LegPersonAttributePredicate(MiDKeys.PERSON_LAU2_CLASS, String.valueOf(klass));
            Predicate<Segment> predicate = PredicateAndComposite.create(engine.getLegPredicate(), lauPred);
            String label = String.format("car.lau%s", klass);
            composite.addComponent(NumericLegAnalyzer.create(
                    Attributes.KEY.BEELINE_DISTANCE,
                    engine.getUseWeights(),
                    predicate,
                    label,
                    histogramWriter));
        }

        Predicate<Segment> inTown = new LegAttributePredicate(MiDKeys.LEG_DESTINATION, MiDValues.IN_TOWN);
        Predicate<Segment> predicate = PredicateAndComposite.create(engine.getLegPredicate(), inTown);
        composite.addComponent(NumericLegAnalyzer.create(
                Attributes.KEY.BEELINE_DISTANCE, engine.getUseWeights(),
                predicate,
                engine.getLegPredicateName() + ".inTown",
                histogramWriter));

        Predicate<Segment> outOfTown = new LegAttributePredicate(MiDKeys.LEG_DESTINATION, MiDValues.OUT_OF_TOWN);
        predicate = PredicateAndComposite.create(engine.getLegPredicate(), outOfTown);
        composite.addComponent(NumericLegAnalyzer.create(
                Attributes.KEY.BEELINE_DISTANCE,
                engine.getUseWeights(),
                predicate,
                engine.getLegPredicateName() + ".outOfTown",
                histogramWriter));

//        LegCollector<String> purposeCollector = new LegCollector<>(new AttributeProvider<Segment>(CommonKeys.LEG_PURPOSE));
//        purposeCollector.setLegPredicate(engine.getLegPredicate());
//        Set<String> purposes = new HashSet<>(purposeCollector.collect(engine.getRefPersons()));
//        purposes.remove(null);
//        for (String purpose : purposes) {
//            Predicate<Segment> purposePredicate = new LegAttributePredicate(CommonKeys.LEG_PURPOSE, purpose);
//            predicate = PredicateAndComposite.create(engine.getLegPredicate(), purposePredicate);
//            composite.addComponent(NumericLegAnalyzer.create(
//                    CommonKeys.LEG_GEO_DISTANCE,
//                    engine.getUseWeights(),
//                    predicate,
//                    String.format("%s.%s", engine.getLegPredicateName(), purpose),
//                    histogramWriter));
//        }

        task.addComponent(new AnalyzerTaskGroup<>(composite, engine.getIOContext(), "distance"));

        /*
        mobility rate
         */
        ZoneCollection lau2Zones = ((ZoneData) engine.getDataPool().get(ZoneDataLoader.KEY)).getLayer("lau2");
        ZoneMobilityRate zoneMobilityRate = new ZoneMobilityRate(
                MiDKeys.PERSON_LAU2_CLASS,
                lau2Zones,
                engine.getLegPredicate(),
                engine.getIOContext());
        task.addComponent(zoneMobilityRate);
        /*
        person weights
         */
        task.addComponent(new NumericAnalyzer(new PersonCollector<>(
                new NumericAttributeProvider<Person>(Attributes.KEY.WEIGHT)),
                "weights",
                new HistogramWriter(
                        engine.getIOContext(),
                        new PassThroughDiscretizerBuilder(new LinearDiscretizer(1), "linear"))));
        /*
        mean geo distance over number of legs per episode
         */
        task.addComponent(new GeoDistNumTripsTask(engine.getIOContext(), engine.getLegPredicate()));
        /*
        trips per person
         */
        task.addComponent(new TripsPerPersonTask().build(engine.getIOContext()));
        /*
        leg purposes over distance
         */
        /*
        Create the geo distance discretizer.
         */
        TDoubleArrayList borders = new TDoubleArrayList();
        borders.add(-1);
        for (int d = 2000; d < 10000; d += 2000) borders.add(d);
        for (int d = 10000; d < 50000; d += 10000) borders.add(d);
        for (int d = 50000; d < 500000; d += 50000) borders.add(d);
        for (int d = 500000; d < 1000000; d += 100000) borders.add(d);
        borders.add(Double.MAX_VALUE);
        Discretizer discretizer = new FixedBordersDiscretizer(borders.toArray());

        LegPurposeDistanceTask lpdTask = new LegPurposeDistanceTask();
        lpdTask.setPredicate(engine.getLegPredicate());
        lpdTask.setIoContext(engine.getIOContext());
        lpdTask.setDiscretizer(discretizer);
        task.addComponent(lpdTask);

        ActTypeDistanceTask atdTask = new ActTypeDistanceTask();
        atdTask.setPredicate(engine.getLegPredicate());
        atdTask.setIoContext(engine.getIOContext());
        atdTask.setDiscretizer(discretizer);
        task.addComponent(atdTask);

        ActTypeDistanceTask atdTask2 = new ActTypeDistanceTask();
        atdTask2.setPrevActMode(true);
        atdTask2.setPredicate(engine.getLegPredicate());
        atdTask2.setIoContext(engine.getIOContext());
        atdTask2.setDiscretizer(discretizer);
        task.addComponent(atdTask2);
        /*

         */
        TaskRunner.run(new SetSeason(), engine.getRefPersons());
        task.addComponent(new SeasonTask(engine.getIOContext()));

    }
}
