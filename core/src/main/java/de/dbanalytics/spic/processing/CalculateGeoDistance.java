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
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;

/**
 * @author johannes
 */
public class CalculateGeoDistance implements EpisodeTask {

    private final PlaceIndex placeIndex;

    public CalculateGeoDistance(PlaceIndex placeIndex) {
        this.placeIndex = placeIndex;
    }

    @Override
    public void apply(Episode episode) {
        for(int i = 0; i < episode.getLegs().size(); i++) {
            Segment from = episode.getActivities().get(i);
            Segment to = episode.getActivities().get(i + 1);

            String idFrom = from.getAttribute(CommonKeys.PLACE);
            String idTo = to.getAttribute(CommonKeys.PLACE);

            Place placeFrom = placeIndex.get(idFrom);
            Place placeTo = placeIndex.get(idTo);

            double dx = placeFrom.getGeometry().getCoordinate().x - placeTo.getGeometry().getCoordinate().x;
            double dy = placeFrom.getGeometry().getCoordinate().y - placeTo.getGeometry().getCoordinate().y;
            double d = Math.sqrt(dx*dx + dy*dy);

            Segment leg = episode.getLegs().get(i);
            leg.setAttribute(CommonKeys.BEELINE_DISTANCE, String.valueOf(d));
        }
    }
}
