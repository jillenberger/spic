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

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.PlainPerson;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.mid2008.generator.*;
import de.dbanalytics.spic.processing.IsolateEpisodes;
import de.dbanalytics.spic.processing.TaskRunner;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author johannes
 */
public class Generator {

    public static final String MODULE_NAME = "mid2008Generator";
    public static final String PERSONS_FILE = "personsFile";
    public static final String TRIPS_FILE = "tripsFile";
    public static final String JOURNEYS_FILE = "journeysFile";
    public static final String PURPOSE_MAPPING_FILE = "purposeMappingFile";
    public static final String OUTPUT_DIR = "output";
    private static final Logger logger = Logger.getLogger(Generator.class);

    public static void main(String args[]) throws IOException {
        Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);

        String personsFile = config.getParam(MODULE_NAME, PERSONS_FILE);
        String tripsFile = config.getParam(MODULE_NAME, TRIPS_FILE);
        String journeysFile = config.getParam(MODULE_NAME, JOURNEYS_FILE);
        String purposeMappingFile = config.getModule(MODULE_NAME).getValue(PURPOSE_MAPPING_FILE);
        String outDir = config.getParam(MODULE_NAME, OUTPUT_DIR);

        PlainFactory factory = new PlainFactory();
        FileReader fileReader = new FileReader(factory);

        fileReader.addPersonAttributeHandler(new PersonAgeHandler());
        fileReader.addPersonAttributeHandler(new PersonCarAvailHandler());
        fileReader.addPersonAttributeHandler(new PersonDayHandler());
        fileReader.addPersonAttributeHandler(new PersonHHIncomeHandler());
        fileReader.addPersonAttributeHandler(new PersonHHMembersHandler());
        fileReader.addPersonAttributeHandler(new PersonMonthHandler());
        fileReader.addPersonAttributeHandler(new PersonMunicipalityClassHandler());
        fileReader.addPersonAttributeHandler(new PersonSexHandler());
        fileReader.addPersonAttributeHandler(new PersonNUTS1Handler());
        fileReader.addPersonAttributeHandler(new PersonWeightHandler());
        fileReader.addPersonAttributeHandler(new PersonDistrictHandler());

        fileReader.addLegAttributeHandler(new LegDistanceHandler());
        fileReader.addLegAttributeHandler(new LegTimeHandler());
        fileReader.addLegAttributeHandler(new LegPurposeHandler(LegPurposeHandler.loadMappingFromFile(purposeMappingFile)));
        fileReader.addLegAttributeHandler(new LegDestinationHandler());
        fileReader.addLegAttributeHandler(new LegOriginHandler());
        fileReader.addLegAttributeHandler(new LegModeHandler());
        fileReader.addLegAttributeHandler(new LegIndexHandler());

        fileReader.addJourneyAttributeHandler(new JourneyDistanceHandler());
        fileReader.addJourneyAttributeHandler(new JourneyModeHandler());
        fileReader.addJourneyAttributeHandler(new JourneyPurposeHandler());
        fileReader.addJourneyAttributeHandler(new JourneyDestinationHandler());

        fileReader.addEpisodeAttributeHandler(new JourneyDaysHandler());

        logger.info("Generating persons...");
        Set<PlainPerson> persons = (Set<PlainPerson>) fileReader.read(personsFile, tripsFile, journeysFile);
        logger.info(String.format("Generated %s persons.", persons.size()));

        logger.info("Inserting dummy activities...");
        TaskRunner.run(new InsertActivitiesTask(factory), persons);

        logger.info("Writing persons...");
        PopulationIO.writeToXML(String.format("%s/mid2008.xml", outDir), persons);

        logger.info("Isolating persons...");
        IsolateEpisodes isolator = new IsolateEpisodes(CommonKeys.DATA_SOURCE, factory);
        TaskRunner.run(isolator, persons);
        Map<String, Set<Person>> populations = isolator.getPopulations();
        for (Map.Entry<String, Set<Person>> entry : populations.entrySet()) {
            logger.info(String.format("Writing persons %s...", entry.getKey()));
            PopulationIO.writeToXML(String.format("%s/mid2008.%s.xml", outDir, entry.getKey()), entry.getValue());
        }

        logger.info("Done.");
    }
}
