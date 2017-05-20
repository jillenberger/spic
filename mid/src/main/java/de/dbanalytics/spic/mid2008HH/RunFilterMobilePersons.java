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

import de.dbanalytics.spic.analysis.MobilePersonPredicate;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.processing.ValidatePerson;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 * Created by johannesillenberger on 10.05.17.
 */
public class RunFilterMobilePersons {

    private final static Logger logger = Logger.getLogger(RunFilterMobilePersons.class);

    public static void main(String args[]) {
        String inFile = args[0];
        String outFile = args[1];

        logger.info("Loading persons...");
        Set<Person> persons = PopulationIO.loadFromXML(inFile, new PlainFactory());
        TaskRunner.validatePersons(new ValidatePerson(new MobilePersonPredicate<>()), persons);
        logger.info(String.format("Writing %s persons...", persons.size()));
        PopulationIO.writeToXML(outFile, persons);
        logger.info("Done.");
    }
}
