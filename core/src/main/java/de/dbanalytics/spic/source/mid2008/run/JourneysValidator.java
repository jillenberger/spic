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

package de.dbanalytics.spic.source.mid2008.run;

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
import org.apache.log4j.Logger;

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
