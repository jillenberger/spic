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

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.EpisodeTask;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author jillenberger
 */
public class RefPopulationBuilderDrive {

    private static final Logger logger = Logger.getLogger(RefPopulationBuilderDrive.class);

    public static Set<? extends Person> build(Simulator engine, Config config) {
        logger.info("Loading persons...");
        Set<Person> refPersons = PopulationIO.loadFromXML(config.findParam(engine.MODULE_NAME, "popInputFile"), new PlainFactory());
        logger.info(String.format("Loaded %s persons.", refPersons.size()));

//        logger.info("Preparing reference simulation...");
//        TaskRunner.validatePersons(new ValidateMissingAttribute(CommonKeys.PERSON_WEIGHT), refPersons);
//        TaskRunner.validatePersons(new ValidatePersonWeight(), refPersons);
//
//        TaskRunner.run(new SetVacationsPurpose(), refPersons);
//        TaskRunner.run(new ReplaceHomePurpose(), refPersons);
//        TaskRunner.run(new RemoveLegPurpose(ActivityTypes.HOME), refPersons);
//        TaskRunner.run(new RemoveLegPurpose(ActivityTypes.MISC), refPersons);
////        TaskRunner.run(new ReplaceLegPurposes(), refPersons);
//        TaskRunner.run(new GuessMissingPurposes(refPersons, engine.getLegPredicate(), engine.getRandom()), refPersons);
//
//        TaskRunner.run(new SetNonHomeActTypes(), refPersons);
////        TaskRunner.run(new ReplaceActTypes(), refPersons);
//        new GuessMissingActTypes(engine.getRandom()).apply(refPersons);
//        TaskRunner.run(new Route2GeoDistance(new de.dbanalytics.devel.matrix2014.sim.Simulator.Route2GeoDistFunction()), refPersons);
//
//        TaskRunner.runActTask(new RandomizeNumAttribute(CommonKeys.ACTIVITY_START_TIME, 300, engine.getRandom()), refPersons);
//        TaskRunner.runLegTask(new SnapLeg2ActTimes(), refPersons);

        return refPersons;
    }

    public static class SetVacationsPurpose implements EpisodeTask {

        @Override
        public void apply(Episode episode) {
            for(Segment act : episode.getActivities()) {
                if(ActivityTypes.VACATIONS_LONG.equalsIgnoreCase(act.getAttribute(CommonKeys.ACTIVITY_TYPE))) {
                    replace(act, ActivityTypes.VACATIONS_LONG);
                }

                if(ActivityTypes.VACATIONS_SHORT.equalsIgnoreCase(act.getAttribute(CommonKeys.ACTIVITY_TYPE))) {
                    replace(act, ActivityTypes.VACATIONS_SHORT);
                }
            }
        }

        private void replace(Segment act, String type) {
            Segment prev = act.previous();
            Segment next = act.next();

            if(prev != null) {
                prev.setAttribute(CommonKeys.LEG_PURPOSE, type);
            }

            if(next != null) {
                next.setAttribute(CommonKeys.LEG_PURPOSE, type);
            }
        }
    }

    public static class ReplaceLegPurposes implements EpisodeTask {

        private Map<String, String> mapping;

        private Map<String, String> getMapping() {
            if(mapping == null) {
                mapping = new HashMap<>();

                mapping.put(ActivityTypes.PRIVATE, ActivityTypes.LEISURE);
                mapping.put(ActivityTypes.PICKDROP, ActivityTypes.LEISURE);
                mapping.put(ActivityTypes.GASTRO, ActivityTypes.LEISURE);
                mapping.put(ActivityTypes.CULTURE, ActivityTypes.LEISURE);
                mapping.put(ActivityTypes.VISIT, ActivityTypes.LEISURE);
            }

            return mapping;
        }

        @Override
        public void apply(Episode episode) {
            for(Segment leg : episode.getLegs()) {
                String purpose = leg.getAttribute(CommonKeys.LEG_PURPOSE);
                String replace = getMapping().get(purpose);
                if(replace != null) leg.setAttribute(CommonKeys.LEG_PURPOSE, replace);
            }
        }
    }

    private static class SetNonHomeActTypes implements EpisodeTask {

        @Override
        public void apply(Episode episode) {
            for(int i = 1; i < episode.getActivities().size(); i++) {
                Segment act = episode.getActivities().get(i);
                if(!ActivityTypes.HOME.equalsIgnoreCase(act.getAttribute(CommonKeys.ACTIVITY_TYPE))) {
                    String purpose = act.previous().getAttribute(CommonKeys.LEG_PURPOSE);
                    act.setAttribute(CommonKeys.ACTIVITY_TYPE, purpose);
                }
            }
        }
    }
}
