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

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.mid2008.MidAttributes;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 */
public class SetFirstActivityTypeTask implements EpisodeTask {

    @Override
    public void apply(Episode episode) {
        if (episode.getLegs().size() > 0) {
            Attributable firstLeg = episode.getLegs().get(0);
            Attributable firstAct = episode.getActivities().get(0);

            String origin = firstLeg.getAttribute(MidAttributes.KEY.ORIGIN);
            if (ActivityTypes.HOME.equals(origin) || ActivityTypes.WORK.equals(origin)) {
                firstAct.setAttribute(Attributes.KEY.TYPE, origin);
            }

        }
    }

}
