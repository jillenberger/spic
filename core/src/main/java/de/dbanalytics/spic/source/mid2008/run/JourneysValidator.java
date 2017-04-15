/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.spic.source.mid2008.run;

import org.apache.log4j.Logger;
import de.dbanalytics.spic.data.Factory;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.SetActivityTypeTask;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.processing.ValidateNoPlans;
import de.dbanalytics.spic.source.mid2008.processing.SetFirstActivityTypeTask;
import de.dbanalytics.spic.source.mid2008.processing.VacationsTypeTask;
import de.dbanalytics.spic.source.mid2008.processing.ValidateDomestic;

import java.util.Set;

/**
 * @author johannes
 */
public class JourneysValidator {

    private static final Logger logger = Logger.getLogger(JourneysValidator.class);

    public static final void main(String args[]) {
        Factory factory = new PlainFactory();
        Set<? extends Person> persons = PopulationIO.loadFromXML(args[0], factory);

        TaskRunner.validateEpisodes(new ValidateDomestic(), persons);
        TaskRunner.validatePersons(new ValidateNoPlans(), persons);

        logger.info("Setting activity types...");
        TaskRunner.run(new SetActivityTypeTask(), persons);
        logger.info("Setting first activity type...");
        TaskRunner.run(new SetFirstActivityTypeTask(), persons);
        logger.info("Setting vacations type...");
        TaskRunner.run(new VacationsTypeTask(), persons);

        logger.info("Writing validated population...");
        PopulationIO.writeToXML(args[1], persons);
        logger.info("Done.");
    }
}
