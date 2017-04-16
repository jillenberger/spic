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

package de.dbanalytics.spic.source.invermo.run;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.source.invermo.generator.*;
import de.dbanalytics.spic.source.invermo.processing.*;
import de.dbanalytics.spic.source.mid2008.generator.InsertActivitiesTask;
import org.apache.log4j.Logger;

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
