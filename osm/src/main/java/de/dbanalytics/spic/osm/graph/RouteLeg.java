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

package de.dbanalytics.spic.osm.graph;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.GeoTransformer;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.processing.SegmentTask;
import gnu.trove.list.array.TLongArrayList;

/**
 * @author johannes
 */
public class RouteLeg implements SegmentTask {

    private static final String PATH_SEPARATOR = " ";

    private final RoutingService router;

    private final PlaceIndex placeIndex;

    private final GeoTransformer transformer;

    public RouteLeg(RoutingService router, PlaceIndex placeIndex, GeoTransformer transformer) {
        this.router = router;
        this.placeIndex = placeIndex;
        this.transformer = transformer;
    }

    @Override
    public void apply(Segment segment) {
        Segment from = segment.previous();
        Segment to = segment.next();

        String fromPlaceId = from.getAttribute(CommonKeys.ACTIVITY_FACILITY);
        String toPlaceId = to.getAttribute(CommonKeys.ACTIVITY_FACILITY);

        Place fromPlace = placeIndex.get(fromPlaceId);
        Place toPlace = placeIndex.get(toPlaceId);

        double[] fromCoord = new double[]{
                fromPlace.getGeometry().getCoordinate().x,
                fromPlace.getGeometry().getCoordinate().y};

        double[] toCoord = new double[]{
                toPlace.getGeometry().getCoordinate().x,
                toPlace.getGeometry().getCoordinate().y};

        transformer.backward(fromCoord);
        transformer.backward(toCoord);

        RoutingResult result = router.query(fromCoord[1], fromCoord[0], toCoord[1], toCoord[0]);

        if (result != null) {
            TLongArrayList nodes = result.getPath();

            StringBuilder builder = new StringBuilder(1000);
            for (int i = 0; i < nodes.size(); i++) {
                if (i > 0) builder.append(PATH_SEPARATOR);
                builder.append(String.valueOf(nodes.get(i)));
            }

            segment.setAttribute(CommonKeys.LEG_ROUTE, builder.toString());
        }
    }
}
