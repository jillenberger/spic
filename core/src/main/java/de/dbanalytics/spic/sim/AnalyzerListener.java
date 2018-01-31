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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.AnalyzerTaskRunner;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author johannes
 */
public class AnalyzerListener implements McmcSimulationObserver {

    private static final Logger logger = Logger.getLogger(AnalyzerListener.class);

    private final AnalyzerTask task;

    private final long interval;

    private final AtomicLong iters = new AtomicLong();

    private final FileIOContext ioContext;

    private final DecimalFormat df;

    public AnalyzerListener(AnalyzerTask task, FileIOContext ioContext, long interval) {
        this.ioContext = ioContext;
        this.interval = interval;
        this.task = task;
        df = new DecimalFormat("0E0", new DecimalFormatSymbols(Locale.US));
        df.setMaximumFractionDigits(340);
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        if (iters.get() % interval == 0) {
            logger.debug("Analyzing simulation population...");
            ioContext.append(df.format(iters.get()));
            AnalyzerTaskRunner.run(population, task, ioContext);
            logger.debug("Done.");
        }
        iters.incrementAndGet();
    }
}
