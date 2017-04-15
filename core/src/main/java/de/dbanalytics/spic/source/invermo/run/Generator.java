/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
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

package de.dbanalytics.spic.source.invermo.run;

import org.apache.log4j.Logger;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.source.invermo.generator.*;
import de.dbanalytics.spic.source.invermo.processing.*;
import de.dbanalytics.spic.source.mid2008.generator.InsertActivitiesTask;

import java.io.IOException;
import java.util.Collection;

/**
 * @author johannes
 */
public class Generator {

    private static final Logger logger = Logger.getLogger(Generator.class);

    public static void main(String args[]) throws IOException {
        String inDir = "/Users/johannes/gsv/germany-scenario/invermo/txt-utf8/";
        String outFile = "/Users/johannes/gsv/germany-scenario/invermo/pop2/pop.xml";
        /*
        Generate persons...
         */
        logger.info("Loading files...");
        FileReader reader = new FileReader(new PlainFactory());

        reader.addHousholdAttributeHandler(new HouseholdLocationHandler());
        reader.addHousholdAttributeHandler(new HouseholdWeigthHandler());
        reader.addPersonAttributeHandler(new WorkLocationHandler());
        reader.addLegAttributeHandler(new LegStartLocHandler());
        reader.addLegAttributeHandler(new LegDestinationLocHandler());
        reader.addLegAttributeHandler(new LegEndTimeHandler());
        reader.addLegAttributeHandler(new LegStartTimeHandler());
        reader.addLegAttributeHandler(new LegModeHandler());
        reader.addLegAttributeHandler(new LegPurposeHandler());

        Collection<Person> persons = reader.read(inDir);

        logger.info(String.format("Parsed %s persons.", persons.size()));
        /*
        Process persons...
         */
        logger.info("Processing persons...");
        TaskRunner.run(new InsertActivitiesTask(new PlainFactory()), persons);
        TaskRunner.run(new ValidateDatesTask(), persons);
        TaskRunner.run(new ComposeTimeTask(), persons);
        TaskRunner.run(new SetActivityLocations(), persons);
        TaskRunner.run(new CleanLegLocations(), persons);
        TaskRunner.run(new SetActivityTypes(), persons);
        TaskRunner.run(new InfereVacationsType(), persons);
        TaskRunner.run(new SplitPlanTask(), persons);
        TaskRunner.run(new InsertHomePlanTask(), persons);
        TaskRunner.run(new ReplaceLocationAliasTask(), persons);
        TaskRunner.run(new Date2TimeTask(), persons);
        TaskRunner.run(new CopyDate2PersonTask(), persons);

        logger.info("Writing persons...");
        PopulationIO.writeToXML(outFile, persons);

        logger.info("Done.");
    }
}
