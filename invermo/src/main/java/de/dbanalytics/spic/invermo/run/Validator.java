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

package de.dbanalytics.spic.invermo.run;


import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.invermo.processing.CalcGeoDistance;
import de.dbanalytics.spic.invermo.processing.GeocodeLocationsTask;
import de.dbanalytics.spic.invermo.processing.ValidateNoLegs;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.processing.ValidateMissingAttribute;
import de.dbanalytics.spic.processing.ValidateNoPlans;
import de.dbanalytics.spic.processing.ValidatePersonWeight;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * @author johannes
 */
public class Validator {

    private static final Logger logger = Logger.getLogger(Validator.class);

    public static void main(String args[]) {
        String inFile = "/Users/johannes/gsv/germany-scenario/invermo/pop2/pop.xml";
        String outFile = "/Users/johannes/gsv/germany-scenario/invermo/pop2/pop.validated.xml";

        Set<Person> persons = PopulationIO.loadFromXML(inFile, new PlainFactory());

        TaskRunner.validatePersons(new ValidateMissingAttribute(Attributes.KEY.WEIGHT), persons);
        TaskRunner.validatePersons(new ValidatePersonWeight(), persons);
        TaskRunner.validatePersons(new ValidateNoLegs(), persons);
        TaskRunner.validatePersons(new ValidateNoPlans(), persons);

        logger.setLevel(Level.INFO);
        GeocodeLocationsTask geoTask = new GeocodeLocationsTask("localhost", 3128);
        geoTask.setCacheFile("/Users/johannes/gsv/germany-scenario/invermo/txt-utf8/geocache.txt");
        TaskRunner.run(geoTask, persons);
        geoTask.writeCache();
        logger.setLevel(Level.ALL);

        TaskRunner.run(new CalcGeoDistance(), persons);

        logger.info(String.format("Writing %s persons...", persons.size()));
        PopulationIO.writeToXML(outFile, persons);

    }
}
