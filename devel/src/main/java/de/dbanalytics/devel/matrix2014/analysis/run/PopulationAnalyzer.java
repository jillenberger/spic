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

package de.dbanalytics.devel.matrix2014.analysis.run;

import de.dbanalytics.devel.matrix2014.analysis.SetSeason;
import de.dbanalytics.devel.matrix2014.data.ReplaceActTypes;
import de.dbanalytics.devel.matrix2014.matrix.postprocess.SeasonTask;
import de.dbanalytics.devel.matrix2014.sim.run.RefPopulationBuilder;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.DataPool;
import de.dbanalytics.spic.gis.FacilityDataLoader;
import de.dbanalytics.spic.gis.ZoneDataLoader;
import de.dbanalytics.spic.processing.*;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;

import java.util.Collection;
import java.util.Random;
import java.util.Set;

/**
 * @author johannes
 */
public class PopulationAnalyzer {

    private static final String MODULE_NAME = "synPopSim";

    private static final Logger logger = Logger.getLogger(PopulationAnalyzer.class);

    public static void main(String args[]) {
        Logger.getRootLogger().setLevel(Level.TRACE);

        final Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);

        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /*
        Load parameters...
         */
        Random random = new XORShiftRandom(Long.parseLong(config.getParam("global", "randomSeed")));
        FileIOContext ioContext = new FileIOContext(configGroup.getValue("output"));
        /*
        Load GIS data...
         */
        DataPool dataPool = new DataPool();
        dataPool.register(new FacilityDataLoader(configGroup.getValue("facilities"), null, random), FacilityDataLoader.KEY);
        dataPool.register(new ZoneDataLoader(configGroup), ZoneDataLoader.KEY);

//        ValidateFacilities.validate(dataPool, "nuts3");

//        ZoneCollection lau2Zones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("lau2");
//        new ZoneSetLAU2Class().apply(lau2Zones);
        /*
        Load population...
         */
        logger.info("Loading persons...");
        Set<Person> persons = PopulationIO.loadFromXML(config.findParam(MODULE_NAME, "popInputFile"), new
                PlainFactory());
        logger.info(String.format("Loaded %s persons.", persons.size()));

        logger.info("Validating persons...");
        TaskRunner.validatePersons(new ValidateMissingAttribute(CommonKeys.PERSON_WEIGHT), persons);
        TaskRunner.validatePersons(new ValidatePersonWeight(), persons);

        Predicate<Segment> carPredicate = new LegAttributePredicate(CommonKeys.LEG_MODE, CommonValues.LEG_MODE_CAR);

        TaskRunner.run(new RefPopulationBuilder.SetVacationsPurpose(), persons);
        TaskRunner.run(new RefPopulationBuilder.ReplaceHomePurpose(), persons);
        TaskRunner.run(new RefPopulationBuilder.NullifyPurpose(ActivityTypes.HOME), persons);
        TaskRunner.run(new RefPopulationBuilder.NullifyPurpose(ActivityTypes.MISC), persons);
        TaskRunner.run(new RefPopulationBuilder.ReplaceLegPurposes(), persons);
        TaskRunner.run(new RefPopulationBuilder.GuessMissingPurposes(persons, carPredicate, random), persons);

        TaskRunner.run(new ReplaceActTypes(), persons);
        new GuessMissingActTypes(random).apply(persons);
        TaskRunner.run(new Route2GeoDistance(new de.dbanalytics.devel.matrix2014.sim.Simulator.Route2GeoDistFunction()), persons);

        TaskRunner.run(new WeCommuterSynthesizer(), persons);
        /*
        Build analyzer...
         */

        AnalyzerTaskComposite<Collection<? extends Person>> tasks = new AnalyzerTaskComposite<>();

        TaskRunner.run(new SetSeason(), persons);

        tasks.addComponent(new SeasonTask(ioContext));
        tasks.addComponent(new DayTask(ioContext));

        AnalyzerTaskRunner.run(persons, tasks, ioContext);

        Executor.shutdown();
        logger.info("Done.");
    }

    public static class WeCommuterSynthesizer implements EpisodeTask {

        @Override
        public void apply(Episode episode) {
            for(Segment leg : episode.getLegs()) {
                String dist = leg.getAttribute(CommonKeys.LEG_GEO_DISTANCE);
                String purpose = leg.getAttribute(CommonKeys.LEG_PURPOSE);
                if(dist != null && purpose != null) {
                    double d = Double.parseDouble(dist);
                    if(d > 100000 && purpose.equalsIgnoreCase(ActivityTypes.WORK)) {
                        leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.WECOMMUTER);
                    }
                }
            }
        }
    }
}
