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

package de.dbanalytics.devel.matrix2014.data;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.devel.matrix2014.gis.FacilityData;
import de.dbanalytics.devel.matrix2014.gis.FacilityUtils;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculatorFactory;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilitiesImpl;
import org.matsim.facilities.MatsimFacilitiesReader;

import java.util.Set;

/**
 * @author johannes
 */
public class RouteLegs {

    private static final Logger logger = Logger.getLogger(RouteLegs.class);

    private static final String MODULE_NAME = "routeLegs";

    private static final String NETWORK_FILE = "networkFile";

    private static final String FACILITIES_FILE = "facilitiesFile";

    private static final String PERSONS_FILE = "personsInFile";

    private static final String PERSONS_OUT_FILE = "personsOutFile";

    public static void main(String args[]) {
        Config config = ConfigUtils.createConfig();
        ConfigUtils.loadConfig(config, args[0]);
        ConfigGroup group = config.getModule(MODULE_NAME);
        /*
        Load network.
         */
        Scenario scenario = ScenarioUtils.createScenario(config);
        MatsimNetworkReader networkReader = new MatsimNetworkReader(scenario.getNetwork());
        networkReader.readFile(group.getValue(NETWORK_FILE));
        /*
        Load facilities.
         */
        MatsimFacilitiesReader facilitiesReader = new MatsimFacilitiesReader(scenario);
        facilitiesReader.readFile(group.getValue(FACILITIES_FILE));
        FacilityData facilityData = new FacilityData(scenario.getActivityFacilities(), null, new XORShiftRandom());
        /*
        Load persons.
         */
        logger.info("Loading persons...");
        Set<Person> persons = PopulationIO.loadFromXML(group.getValue(PERSONS_FILE), new PlainFactory());
        /*
        Preprocess...
         */
        logger.info("Prerocess routing...");
        int nThreads = Executor.getFreePoolSize();

        LeastCostPathCalculatorFactory factory = new AStarLandmarksFactory(scenario.getNetwork(), new ConcurrentFreeSpeedRouter.TravelTimes());
        ConcurrentFreeSpeedRouter router = new ConcurrentFreeSpeedRouter(scenario.getNetwork(), factory, nThreads);

        logger.info("Connect facilities to network...");
        FacilityUtils.connect2Network((ActivityFacilitiesImpl) facilityData.getAll(), scenario.getNetwork());
        /*
        Run...
         */
        logger.info("Route legs...");
        RouteEpisode task = new RouteEpisode(new RouteLeg(router, facilityData, scenario.getNetwork()));
        TaskRunner.run(task, persons, nThreads, true);
        /*
        Validate...
         */
        int failed = 0;
        int total = 0;
        for(Person p : persons) {
            for(Episode e : p.getEpisodes()) {
                for(Segment l : e.getLegs()) {
                    total++;
                    if(l.getAttribute(CommonKeys.LEG_ROUTE) == null) failed++;
                }
            }
        }

        logger.info(String.format("Calculated %s routes.", total));
        if(failed > 0) logger.warn(String.format("Failed to calculate %s routes.", failed));
        /*
        Write out.
         */
        logger.info("Write persons...");
        PopulationIO.writeToXML(group.getValue(PERSONS_OUT_FILE), persons);

        logger.info("Done.");
        Executor.shutdown();
    }

    private static class RouteEpisode implements EpisodeTask {

        private RouteLeg routeLeg;

        public RouteEpisode(RouteLeg routeLeg) {
            this.routeLeg = routeLeg;
        }

        @Override
        public void apply(Episode episode) {
            for(Segment leg : episode.getLegs()) {
                routeLeg.apply(leg);
            }
        }
    }
}
