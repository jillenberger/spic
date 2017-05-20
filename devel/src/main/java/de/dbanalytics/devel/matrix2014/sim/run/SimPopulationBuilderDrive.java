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

import de.dbanalytics.devel.matrix2014.analysis.ZoneMobilityRate;
import de.dbanalytics.devel.matrix2014.gis.TransferZoneAttribute;
import de.dbanalytics.devel.matrix2014.sim.SetLAU2Attribute;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PersonUtils;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.processing.*;
import de.dbanalytics.spic.sim.SetActivityFacilities;
import de.dbanalytics.spic.sim.SetHomeFacilities;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;

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
        if (simPopFile == null) {

            int size = (int) Double.parseDouble(config.getParam(Simulator.MODULE_NAME, "populationSize"));
            simPersons = (Set<Person>) PersonUtils.weightedCopy(engine.getRefPersons(), new PlainFactory(), size, engine.getRandom());
//            simPersons = (Set<Person>) clonePersons(engine.getRefPersons(),
//                    size,
//                    0.5,
//                    engine.getLegPredicate(),
//                    engine.getRandom());
            logger.info(String.format("Generated %s persons.", simPersons.size()));
            /*
            Initializing simulation population...
            */
            logger.info("Assigning home locations...");
            boolean useZoneWeights = true;
            String val = config.findParam(Simulator.MODULE_NAME, "useZoneWeights");
            if (val != null) {
                useZoneWeights = Boolean.parseBoolean(val);
            }

            SetHomeFacilities setHomeFacilities = new SetHomeFacilities(dataPool, "modena", engine.getRandom());

            if (useZoneWeights) {
                ZoneCollection lau2Zones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("lau2");
                ZoneCollection modenaZones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("modena");

                ZoneMobilityRate zoneMobilityRate = new ZoneMobilityRate(
                        MiDKeys.PERSON_LAU2_CLASS,
                        lau2Zones,
                        engine.getLegPredicate(),
                        engine.getIOContext());
                zoneMobilityRate.analyze(engine.getRefPersons(), null);

                new TransferZoneAttribute().apply(lau2Zones, modenaZones, MiDKeys.PERSON_LAU2_CLASS);
                setHomeFacilities.setZoneWeights(zoneMobilityRate.getMobilityRatePerZone(modenaZones));
            }

            setHomeFacilities.apply(simPersons);

            logger.info("Assigning random activity locations...");
//            TaskRunner.run(new SetActivityFacilities((FacilityData) dataPool.get(FacilityDataLoader.KEY)), simPersons);
            EpisodeTask initActs = new SetActivityFacilities(
                    (FacilityData) dataPool.get(FacilityDataLoader.KEY),
                    0.1,
                    engine.getRandom());
            TaskRunner.run(initActs, simPersons, Executor.getFreePoolSize(), true);
        } else {
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

            //TODO: temp fix!
//            TaskRunner.run(new RefPopulationBuilder.SetVacationsPurpose(), simPersons);
//            TaskRunner.run(new RefPopulationBuilder.ReplaceHomePurpose(), simPersons);
//            TaskRunner.run(new RefPopulationBuilder.RemoveLegPurpose(ActivityTypes.HOME), simPersons);
//            TaskRunner.run(new RefPopulationBuilder.RemoveLegPurpose(ActivityTypes.MISC), simPersons);
//            TaskRunner.run(new RefPopulationBuilder.ReplaceLegPurposes(), simPersons);
//            TaskRunner.run(new RefPopulationBuilder.GuessMissingPurposes(simPersons, engine.getLegPredicate(), engine.getRandom()), simPersons);
        }

        logger.info("Recalculate geo distances...");
        TaskRunner.run(new LegAttributeRemover(CommonKeys.LEG_GEO_DISTANCE), simPersons);
        TaskRunner.run(new CalculateGeoDistance((FacilityData) dataPool.get(FacilityDataLoader.KEY)), simPersons);

        logger.info("Resetting LAU2Class attributes...");
        SetLAU2Attribute lTask = new SetLAU2Attribute(dataPool, "lau2");
        TaskRunner.run(lTask, simPersons);
        if (lTask.getErrors() > 0)
            logger.warn(String.format("Cannot set LAU2Class attribute for %s persons.", lTask.getErrors()));

        return simPersons;
    }
}
