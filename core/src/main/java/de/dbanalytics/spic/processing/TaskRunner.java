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

package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.collections.CollectionUtils;
import org.matsim.contrib.common.util.LoggerUtils;
import org.matsim.contrib.common.util.ProgressLogger;

import java.util.*;

/**
 * @author johannes
 */
public class TaskRunner {

    private static final Logger logger = Logger.getLogger(TaskRunner.class);

    public static void run(PersonTask task, Collection<? extends Person> persons) {
        for (Person person : persons) task.apply(person);
    }

    public static void run(EpisodeTask task, Collection<? extends Person> persons) {
        run(task, persons, false);
    }

    public static void run(EpisodeTask task, Collection<? extends Person> persons, boolean verbose) {
        run(task, persons, verbose, null);
    }

    public static void run(EpisodeTask task, Collection<? extends Person> persons, boolean verbose, Predicate<Episode> predicate) {
        if (verbose) {
            ProgressLogger.init(persons.size(), 2, 10);
        }

        for (Person person : persons) {
            for (Episode plan : person.getEpisodes())
                if (predicate == null || predicate.test(plan)) task.apply(plan);

            if (verbose)
                ProgressLogger.step();
        }

        if (verbose)
            ProgressLogger.terminate();
    }

    public static void run(EpisodeTask task, Collection<? extends Person> persons, int nThreads, boolean verbose) {
        if (verbose) {
            ProgressLogger.init(persons.size(), 2, 10);
        }

        List<? extends Person>[] segments = CollectionUtils.split(persons, nThreads);
        List<Runnable> threads = new ArrayList<>(nThreads);
        for(int i = 0; i < nThreads; i++) {
            threads.add(new RunThread(segments[i], task, verbose));
        }

        Executor.submitAndWait(threads);

        if (verbose)
            ProgressLogger.terminate();
    }

    public static void runLegTask(SegmentTask task, Collection<? extends Person> persons) {
        run(new EpisodeTask() {
            @Override
            public void apply(Episode episode) {
                for(Segment leg : episode.getLegs()) task.apply(leg);
            }
        }, persons);
    }

    public static void runActTask(SegmentTask task, Collection<? extends Person> persons) {
        run(new EpisodeTask() {
            @Override
            public void apply(Episode episode) {
                for(Segment act : episode.getActivities()) task.apply(act);
            }
        }, persons);
    }

    public static void validatePersons(PersonTask task, Collection<? extends Person> persons) {
        LoggerUtils.disableNewLine();
        logger.info(String.format("Running validator %s...", task.getClass().getSimpleName()));

        Set<Person> delete = new HashSet<>(persons.size());

        run(task, persons);

        for (Person person : persons) {
            if (CommonValues.TRUE.equalsIgnoreCase(person.getAttribute(CommonKeys.DELETE))) {
                delete.add(person);
            }
        }

        for (Person person : delete) {
            persons.remove(person);
        }

        if (delete.size() > 0) System.out.println(String.format(" %s invalid persons.", delete.size()));
        else System.out.println(" ok.");
        LoggerUtils.enableNewLine();
    }

    public static void validateEpisodes(EpisodeTask task, Collection<? extends Person> persons) {
        LoggerUtils.disableNewLine();
        logger.info(String.format("Running validator %s...", task.getClass().getSimpleName()));

        run(task, persons);

        int cnt = 0;
        for (Person person : persons) {
            List<Episode> remove = new ArrayList<>();
            for (Episode plan : person.getEpisodes()) {
                if (CommonValues.TRUE.equalsIgnoreCase(plan.getAttribute(CommonKeys.DELETE))) {
                    remove.add(plan);
                    cnt++;
                }
            }

            for (Episode plan : remove) {
                person.getEpisodes().remove(plan);
            }

        }

        if (cnt > 0) {
            System.out.println(String.format(" %s invalid episodes.", cnt));
        } else {
            System.out.println(" ok.");
        }

        LoggerUtils.enableNewLine();
    }

    private static final class RunThread implements Runnable {

        private final List<? extends Person> persons;

        private final EpisodeTask task;

        private final boolean verbose;

        public RunThread(List<? extends Person> persons, EpisodeTask task, boolean verbose) {
            this.persons = persons;
            this.task = task;
            this.verbose = verbose;
        }

        @Override
        public void run() {
            for (Person person : persons) {
                for (Episode plan : person.getEpisodes())
                    task.apply(plan);

                if (verbose)
                    ProgressLogger.step();
            }
        }
    }
}
