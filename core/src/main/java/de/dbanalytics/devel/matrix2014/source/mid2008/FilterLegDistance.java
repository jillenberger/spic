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

package de.dbanalytics.devel.matrix2014.source.mid2008;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 */
public class FilterLegDistance implements EpisodeTask {

    @Override
    public void apply(Episode episode) {
        for(Segment leg : episode.getLegs()) {
            String value = leg.getAttribute(CommonKeys.LEG_ROUTE_DISTANCE);
            if(value != null) {
                double d = Double.parseDouble(value);
                if(d > 400000) {
                    episode.setAttribute(CommonKeys.DELETE, CommonValues.TRUE);
                    break;
                }
            }
        }
    }
}
