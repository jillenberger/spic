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

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Factory;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.*;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import de.dbanalytics.spic.source.mid2008.processing.ResolveRoundTripsTask;
import de.dbanalytics.spic.source.mid2008.processing.SetFirstActivityTypeTask;
import de.dbanalytics.spic.source.mid2008.processing.SortLegsTask;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.LoggerUtils;

import java.util.Collection;
import java.util.Set;

/**
 * @author johannes
 */
public class TripsValidator {

    private static final Logger logger = Logger.getLogger(TripsValidator.class);

    public static final void main(String args[]) {
        Factory factory = new PlainFactory();
        Set<? extends Person> persons = PopulationIO.loadFromXML(args[0], factory);



        TaskRunner.run(new SortLegsTask(MiDKeys.LEG_INDEX, new SortLegsTask.IntComparator()), persons);
        TaskRunner.validateEpisodes(new ValidateMissingLegTimes(), persons);
        TaskRunner.validateEpisodes(new ValidateNegativeLegDuration(), persons);
        TaskRunner.validateEpisodes(new ValidateOverlappingLegs(), persons);
        TaskRunner.validatePersons(new ValidateNoPlans(), persons);

        logger.info("Setting activity types...");
        TaskRunner.run(new SetActivityTypeTask(), persons);
        logger.info("Setting first activity type...");
        TaskRunner.run(new SetFirstActivityTypeTask(), persons);

        LoggerUtils.disableNewLine();
        logger.info("Resolving round trips...");
        int cnt = countActivities(persons);
        TaskRunner.run(new ResolveRoundTripsTask(factory), persons);
        System.out.println(String.format(" inserted %s activities.", countActivities(persons) - cnt));
        LoggerUtils.enableNewLine();

        logger.info("Setting activity times...");
        TaskRunner.run(new SetActivityTimesTask(), persons);

        logger.info(String.format("Writing %s validated persons...", persons.size()));
        PopulationIO.writeToXML(args[1], persons);
        logger.info("Done.");
    }

    private static int countActivities(Collection<? extends Person> persons) {
        int cnt = 0;
        for (Person p : persons) {
            for (Episode e : p.getEpisodes()) {
                cnt += e.getActivities().size();
            }
        }

        return cnt;
    }
}
