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

package de.dbanalytics.spic.spic2matsim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class ActTimeValidator implements EpisodeTask {

    private static final int DEFAULT_OFFSET = 28800; // 08:00

    private static final int DEFAULT_STEP = 3600; // 1h

    @Override
    public void apply(Episode episode) {
        double offset = DEFAULT_OFFSET;
        for(Segment act : episode.getActivities()) {
            String start = act.getAttribute(CommonKeys.ACTIVITY_START_TIME);
            if(start == null) {
                offset += DEFAULT_STEP;
                act.setAttribute(CommonKeys.ACTIVITY_START_TIME, String.valueOf(offset));
            } else {
                offset = Double.parseDouble(start);
            }

            String end = act.getAttribute(CommonKeys.ACTIVITY_END_TIME);
            if(end == null) {
                offset += DEFAULT_STEP;
                act.setAttribute(CommonKeys.ACTIVITY_END_TIME, String.valueOf(offset));
            } else {
                offset = Double.parseDouble(end);
            }
        }
    }
}
