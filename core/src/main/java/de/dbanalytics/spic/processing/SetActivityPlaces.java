/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import java.util.List;
import java.util.Random;

/**
 * @author johannes
 */
public class SetActivityPlaces implements EpisodeTask {

    private static final Logger logger = Logger.getLogger(SetActivityPlaces.class);

    private final PlaceIndex placeIndex;

    private final Random random;

    private final double rangeFactor;

    private final double fallbackDistance = 10000;

    public SetActivityPlaces(PlaceIndex data) {
        this(data, 0.1, new XORShiftRandom());
    }

    public SetActivityPlaces(PlaceIndex placeIndex, double rangeFactor, Random random) {
        this.placeIndex = placeIndex;
        this.random = random;
        this.rangeFactor = rangeFactor;
    }

    @Override
    public void apply(Episode episode) {
        for (Segment act : episode.getActivities()) {
            if (act.getAttribute(CommonKeys.ACTIVITY_FACILITY) == null) {
                String type = act.getAttribute(CommonKeys.ACTIVITY_TYPE);
                Place place = null;
                Segment toLeg = act.previous();
                /*
                get random facility in distance range
                 */
                if (toLeg != null) {
                    Segment prevAct = toLeg.previous();

                    Place origin = placeIndex.get(prevAct.getAttribute(CommonKeys.ACTIVITY_FACILITY));

                    String distance = toLeg.getAttribute(CommonKeys.LEG_GEO_DISTANCE);
                    double d = fallbackDistance;
                    if (distance != null) d = Double.parseDouble(distance);

                    double range = d * rangeFactor;

                    List<Place> candidates = placeIndex.getForActivity(
                            origin.getGeometry().getCoordinate(),
                            d - range,
                            d + range,
                            type);

                    if (candidates.isEmpty()) {
                        place = placeIndex.getClosestForActivity(origin.getGeometry().getCoordinate(), type);
//                        place = getRandomPlace(type);
                    } else {
                        place = candidates.get(random.nextInt(candidates.size()));
                    }
                }
                /*
                fallback to random facility
                 */
                if (place == null) {
                    place = getRandomPlace(type);
                }

                act.setAttribute(CommonKeys.ACTIVITY_FACILITY, place.getId().toString());
            }
        }
    }

    private Place getRandomPlace(String type) {
        List<Place> list = placeIndex.getForActivity(type);
        if (list != null) {
            return list.get(random.nextInt(list.size()));
        } else {
            logger.warn(String.format("No places found for type \"%s\".", type));
            return null;
        }
    }
}
