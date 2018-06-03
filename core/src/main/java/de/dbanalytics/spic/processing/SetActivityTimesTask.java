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

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;

/**
 * @author jillenberger
 */
public class SetActivityTimesTask implements EpisodeTask {
    @Override
    public void apply(Episode episode) {
        if (episode.getActivities().size() == 1) {
            Segment act = episode.getActivities().get(0);
            act.setAttribute(CommonKeys.START_TIME, "0");
            act.setAttribute(CommonKeys.END_TIME, "86400");
        } else {

            for (int i = 0; i < episode.getActivities().size(); i++) {
                Segment act = episode.getActivities().get(i);

                if (i == 0) act.setAttribute(CommonKeys.START_TIME, "0");
                else
                    act.setAttribute(CommonKeys.START_TIME, act.previous().getAttribute(CommonKeys.ARRIVAL_TIME));

                if (i == episode.getActivities().size() - 1) {
                    int startTime = Integer.parseInt(act.getAttribute(CommonKeys.START_TIME));
                    startTime = Math.max(startTime + 1, 86400);
                    act.setAttribute(CommonKeys.END_TIME, String.valueOf(startTime));
                } else {
                    act.setAttribute(CommonKeys.END_TIME, act.next().getAttribute(CommonKeys.DEPARTURE_TIME));
                }
            }
        }
    }
}
