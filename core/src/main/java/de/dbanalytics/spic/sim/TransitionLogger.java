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

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author johannes
 */
public class TransitionLogger implements McmcSimulationObserver {

    private static final Logger logger = Logger.getLogger(TransitionLogger.class);

    private long acceptedIterations;

    private long rejectedIterations;

    private long interval;

    private long time;

    public TransitionLogger(long interval) {
        this.interval = interval;
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        if(accepted) acceptedIterations++;
        else rejectedIterations++;

        if((acceptedIterations + rejectedIterations) % interval == 0) {
            double stepsPerSec = (acceptedIterations + rejectedIterations)/ (System.currentTimeMillis() - time);
            double ratio = acceptedIterations/(double)(acceptedIterations + rejectedIterations);
            logger.info(String.format("Steps accepted %s, rejected %s, ratio %.4f, steps per msec %s.", acceptedIterations,
                    rejectedIterations, ratio, stepsPerSec));
            acceptedIterations = 0;
            rejectedIterations = 0;
            time = System.currentTimeMillis();
        }
    }
}
