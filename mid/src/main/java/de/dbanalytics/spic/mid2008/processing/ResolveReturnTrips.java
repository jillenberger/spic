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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;
import org.apache.log4j.Logger;

/**
 * Created by johannesillenberger on 18.05.17.
 */
public class ResolveReturnTrips implements EpisodeTask {

    private static final Logger logger = Logger.getLogger(ResolveReturnTrips.class);

    private static final String RETURN_TYPE = "return";

    private boolean verbose = true;

    private int errors = 0;

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public void apply(Episode episode) {
        for (int i = 0; i < episode.getActivities().size(); i++) {
            Segment act = episode.getActivities().get(i);
            if (RETURN_TYPE.equalsIgnoreCase(act.getAttribute(Attributes.KEY.TYPE))) {
                if (i > 1) {
                    Segment startAct = episode.getActivities().get(i - 2);
                    act.setAttribute(Attributes.KEY.TYPE, startAct.getAttribute(Attributes.KEY.TYPE));
                } else {
                    if(verbose) logger.warn("Detected return trip without outward trip.");
                    act.setAttribute(Attributes.KEY.TYPE, null);
                    errors++;
                }
            }
        }
    }

    public int getErrors() {
        return errors;
    }
}
