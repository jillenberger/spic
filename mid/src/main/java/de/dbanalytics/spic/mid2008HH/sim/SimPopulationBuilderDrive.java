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

import de.dbanalytics.spic.analysis.NumericAnalyzer;
import de.dbanalytics.spic.analysis.NumericLegAnalyzer;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.DataPool;
import de.dbanalytics.spic.gis.FacilityData;
import de.dbanalytics.spic.gis.FacilityDataLoader;
import de.dbanalytics.spic.processing.*;
import de.dbanalytics.spic.sim.SetActivityFacilities;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author jillenberger
 */
public class SimPopulationBuilderDrive {

    private static final Logger logger = Logger.getLogger(SimPopulationBuilderDrive.class);

    public static Set<? extends Person> build(Simulator engine, Config config) {
        String simPopFile = config.findParam(Simulator.MODULE_NAME, "simPopulation");
        DataPool dataPool = engine.getDataPool();

        Set<Person> simPersons;

            logger.info("Loading sim population from file...");
            simPersons = PopulationIO.loadFromXML(simPopFile, new PlainFactory());

            logger.info("Assigning random activity locations...");
            EpisodeTask initActs = new SetActivityFacilities(
                    (FacilityData) dataPool.get(FacilityDataLoader.KEY),
                    0.1,
                    engine.getRandom());
            TaskRunner.run(initActs, simPersons, Executor.getFreePoolSize(), true);
            TaskRunner.runActTask(new RandomizeNumAttribute(CommonKeys.ACTIVITY_END_TIME, 300, engine.getRandom()), simPersons);
            TaskRunner.runLegTask(new SnapLeg2ActTimes(), simPersons);


        TaskRunner.runLegTask(new TargetDistanceAttribute(simPersons), simPersons);
        logger.info("Recalculate geo distances...");
        TaskRunner.run(new RemoveLegAttribute(CommonKeys.LEG_GEO_DISTANCE), simPersons);
        TaskRunner.run(new CalculateGeoDistance((FacilityData) dataPool.get(FacilityDataLoader.KEY)), simPersons);


        return simPersons;
    }

    private static class TargetDistanceAttribute implements SegmentTask {

        private final double fallbackDistance;

        public TargetDistanceAttribute(Collection<? extends Person> persons) {
            NumericAnalyzer analyzer = NumericLegAnalyzer.create(
                    CommonKeys.LEG_GEO_DISTANCE,
                    false,
                    null,
                    null,
                    null);

            List<StatsContainer> stats = new ArrayList<>(0);
            analyzer.analyze(persons, stats);
            fallbackDistance = stats.get(0).getMedian();
        }

        @Override
        public void apply(Segment segment) {
            String value = segment.getAttribute(CommonKeys.LEG_GEO_DISTANCE);
            if (value == null) {
                value = String.valueOf(fallbackDistance);
            }

            segment.setAttribute(TargetDistance.TARGET_GEO_DISTANCE, value);
        }
    }
}
