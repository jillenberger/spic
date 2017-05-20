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

package de.dbanalytics.spic.mid2008HH;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.mid2008.processing.GuessMissingPurposes;
import de.dbanalytics.spic.mid2008.processing.RemoveLegPurpose;
import de.dbanalytics.spic.mid2008.processing.ReplaceHomePurpose;
import de.dbanalytics.spic.processing.*;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.Random;
import java.util.Set;

/**
 * Created by johannesillenberger on 10.05.17.
 */
public class RunPrepareTemplates {

    private static final Logger logger = Logger.getLogger(RunPrepareTemplates.class);

    public static void main(String args[]) {
        String inFile = args[0];
        String outFile = args[1];

        Random random = new XORShiftRandom();

        logger.info("Loading persons...");
        Set<Person> refPersons = PopulationIO.loadFromXML(inFile, new PlainFactory());
        logger.info(String.format("Loaded %s persons.", refPersons.size()));

        logger.info("Preparing reference simulation...");
        TaskRunner.validatePersons(new ValidateMissingAttribute(CommonKeys.PERSON_WEIGHT), refPersons);
        TaskRunner.validatePersons(new ValidatePersonWeight(), refPersons);
        new FilterWeekday().apply(refPersons);

        TaskRunner.run(new ReplaceHomePurpose(), refPersons);
        TaskRunner.run(new RemoveLegPurpose(ActivityTypes.HOME), refPersons);
        TaskRunner.run(new RemoveLegPurpose(ActivityTypes.MISC), refPersons);
        TaskRunner.run(new GuessMissingPurposes(refPersons, null, random), refPersons);

        new GuessMissingActTypes(random).apply(refPersons);
        TaskRunner.run(new Route2GeoDistance(v -> v * 0.55), refPersons);

        logger.info(String.format("Writing %s persons...", refPersons.size()));
        PopulationIO.writeToXML(outFile, refPersons);
        logger.info("Done.");
    }
}
