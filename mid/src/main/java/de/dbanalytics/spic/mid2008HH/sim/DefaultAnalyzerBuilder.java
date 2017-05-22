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
                CommonKeys.LEG_ROUTE_DISTANCE,
                false,
                null,
                null,
                histogramWriter));

        composite.addComponent(NumericLegAnalyzer.create(
                CommonKeys.LEG_GEO_DISTANCE,
                false,
                null,
                null,
                histogramWriter));


        task.addComponent(new AnalyzerTaskGroup<>(composite, engine.getIOContext(), "distance"));

        /*
        mean geo distance over number of legs per episode
         */
        task.addComponent(new GeoDistNumTripsTask(engine.getIOContext(), null));
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
//        TDoubleArrayList borders = new TDoubleArrayList();
//        borders.add(-1);
//        for (int d = 2000; d < 10000; d += 2000) borders.add(d);
//        for (int d = 10000; d < 50000; d += 10000) borders.add(d);
//        for (int d = 50000; d < 500000; d += 50000) borders.add(d);
//        for (int d = 500000; d < 1000000; d += 100000) borders.add(d);
//        borders.add(Double.MAX_VALUE);
//        Discretizer discretizer = new FixedBordersDiscretizer(borders.toArray());
//
//        LegPurposeDistanceTask lpdTask = new LegPurposeDistanceTask();
//        lpdTask.setPredicate(engine.getLegPredicate());
//        lpdTask.setIoContext(engine.getIOContext());
//        lpdTask.setDiscretizer(discretizer);
//        task.addComponent(lpdTask);
//
//        ActTypeDistanceTask atdTask = new ActTypeDistanceTask();
//        atdTask.setPredicate(engine.getLegPredicate());
//        atdTask.setIoContext(engine.getIOContext());
//        atdTask.setDiscretizer(discretizer);
//        task.addComponent(atdTask);
//
//        ActTypeDistanceTask atdTask2 = new ActTypeDistanceTask();
//        atdTask2.setPrevActMode(true);
//        atdTask2.setPredicate(engine.getLegPredicate());
//        atdTask2.setIoContext(engine.getIOContext());
//        atdTask2.setDiscretizer(discretizer);
//        task.addComponent(atdTask2);
    }
}
