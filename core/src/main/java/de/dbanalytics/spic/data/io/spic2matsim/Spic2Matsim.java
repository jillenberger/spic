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

package de.dbanalytics.spic.data.io.spic2matsim;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.util.PopulationStats;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.FacilitiesReaderMatsimV1;
import org.matsim.utils.objectattributes.ObjectAttributes;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import java.util.Set;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class Spic2Matsim {

    private static final Logger logger = Logger.getLogger(Spic2Matsim.class);

    public static void main(String args[]) {
        String popInFile = args[0];
        String facFile = args[1];
        String popOutFile = args[2];

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);
        /*
        Load persons...
         */
        logger.info("Loading persons...");
        Set<? extends Person> persons = PopulationIO.loadFromXML(popInFile, new PlainFactory());
        /*
        Load facilities...
         */
        logger.info("Loading facilities...");
        FacilitiesReaderMatsimV1 facReader = new FacilitiesReaderMatsimV1(scenario);
        facReader.readFile(facFile);
        ActivityFacilities facilities = scenario.getActivityFacilities();
        /*
        Validate persons...
         */
        logger.info("Validating persons...");
        TaskRunner.run(new ActTimeValidator(), persons);
        TaskRunner.runLegTask(new LegModeValidator(), persons);
        /*
        Convert...
         */
        PopulationStats stats = new PopulationStats();
        stats.run(persons);
        logger.info(String.format("Converting persons %s persons, %s episodes, %s activities and %s legs",
                stats.getNumPersons(),
                stats.getNumEpisodes(),
                stats.getNumActivities(),
                stats.getNumLegs()));
        PersonConverter converter = new PersonConverter(scenario.getPopulation(), facilities);
        converter.convert(persons);

        /*
        Write matsim xml...
         */
        logger.info("Writing population...");
        PopulationWriter writer = new PopulationWriter(scenario.getPopulation(), null);
        writer.write(popOutFile);

        logger.info("Writing person attributes...");
        int idx = popOutFile.lastIndexOf("/");
        String attFile = String.format("%s/attributes.xml.gz", popOutFile.substring(0, idx));
        ObjectAttributes attrs = scenario.getPopulation().getPersonAttributes();
        ObjectAttributesXmlWriter oaWriter = new ObjectAttributesXmlWriter(attrs);
        oaWriter.writeFile(attFile);
    }
}
