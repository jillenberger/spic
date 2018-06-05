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

package de.dbanalytics.devel.matrix2014.analysis.run;

import de.dbanalytics.devel.matrix2014.analysis.SetSeason;
import de.dbanalytics.devel.matrix2014.matrix.postprocess.SeasonTask;
import de.dbanalytics.spic.analysis.AnalyzerTaskComposite;
import de.dbanalytics.spic.analysis.AnalyzerTaskRunner;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.processing.TaskRunner;

import java.util.Set;

/**
 * @author johannes
 */
public class AnalyseInfermo {

    public static void main(String args[]) {
        Set<Person> persons = PopulationIO.loadFromXML("/Users/johannes/gsv/germany-scenario/invermo/pop2/pop.validated.xml", new PlainFactory());

        FileIOContext ioContext = new FileIOContext("/Users/johannes/gsv/germany-scenario/invermo/analysis");

        TaskRunner.run(new InputeDummyDistance(), persons);
        TaskRunner.run(new SetSeason(), persons);

        AnalyzerTaskComposite tasks = new AnalyzerTaskComposite();
        tasks.addComponent(new SeasonTask(ioContext));
        tasks.addComponent(new DayTask(ioContext));

        AnalyzerTaskRunner.run(persons, tasks, ioContext);
    }

    private static class InputeDummyDistance implements EpisodeTask {

        @Override
        public void apply(Episode episode) {
            for(Segment leg : episode.getLegs()) {
                leg.setAttribute(Attributes.KEY.BEELINE_DISTANCE, "100000");
            }
        }
    }

//    private static class InputeLegPurpose implements EpisodeTask {
//
//        @Override
//        public void apply(Episode episode) {
//            for(Segment leg : episode.getLegs()) {
//                if(ActivityTypes.HOME.equalsIgnoreCase(leg.next().getAttribute(CommonKeys.ACTIVITY_TYPE))) {
//                    leg.setAttribute(CommonKeys.LEG_PURPOSE, leg.previous().getAttribute(CommonKeys.ACTIVITY_TYPE));
//                } else {
//                    leg.setAttribute(CommonKeys.LEG_PURPOSE, leg.next().getAttribute(CommonKeys.ACTIVITY_TYPE));
//                }
//            }
//        }
//    }
}
