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
package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jillenberger
 */
public class ConcurrentAnalyzerTask<T> extends AnalyzerTaskComposite<T> {

    private static final Logger logger = Logger.getLogger(ConcurrentAnalyzerTask.class);

    @Override
    public void analyze(final T object, final List<StatsContainer> containers) {
        final List<StatsContainer> concurrentContainers = new CopyOnWriteArrayList<>();

        List<Runnable> runnables = new ArrayList<>(components.size());
        for (final AnalyzerTask<T> task : components) {
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    task.analyze(object, concurrentContainers);
                }
            });
            logger.trace(String.format("Submitting analyzer task %s...", task.getClass().getSimpleName()));
        }

        Executor.submitAndWait(runnables);

        containers.addAll(concurrentContainers);

        logger.trace("Tasks done.");
    }
}
