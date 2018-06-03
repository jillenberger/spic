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
package de.dbanalytics.spic.sim;

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedSegment;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;

/**
 * @author jillenberger
 */
public class GeoDistanceUpdater implements AttributeObserver {

    private final Object geoDistDataKey = Converters.register(CommonKeys.BEELINE_DISTANCE, DoubleConverter.getInstance());

    private Object placeDataKey;

    /** @deprecated */
    private AttributeObserver listener;

    private AttributeMediator mediator;

    private Predicate<CachedSegment> predicate;

    public GeoDistanceUpdater() {
        this.listener = null;
    }

    /** @deprecated */
    public GeoDistanceUpdater(AttributeObserver listener) {
        setListener(listener);
    }

    public GeoDistanceUpdater(AttributeMediator mediator) {
        setMediator(mediator);
    }

    public void setPredicate(Predicate<CachedSegment> predicate) {
        this.predicate = predicate;
    }

    /** @deprecated */
    public void setListener(AttributeObserver listener) {
        this.listener = listener;
    }

    public void setMediator(AttributeMediator mediator) {
        this.mediator = mediator;
    }

    @Override
    public void update(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (placeDataKey == null) placeDataKey = Converters.getObjectKey(CommonKeys.PLACE);

        if (placeDataKey.equals(dataKey)) {
            CachedSegment act = (CachedSegment) element;
            CachedSegment toLeg = (CachedSegment) act.previous();
            CachedSegment fromLeg = (CachedSegment) act.next();

            Place place1 = (Place) act.getData(placeDataKey);
            Coordinate c1 = place1.getGeometry().getCoordinate();

            if (toLeg != null) {
                CachedSegment prevAct = (CachedSegment) toLeg.previous();
                Place place2 = (Place) prevAct.getData(placeDataKey);
                Coordinate c2 = place2.getGeometry().getCoordinate();
                double d = distance(c2, c1);
                //double d = distance(prevAct, act);
                Object old = toLeg.getData(geoDistDataKey);
                toLeg.setData(geoDistDataKey, d);

                if (listener != null) {
                    if (predicate == null || predicate.test(toLeg))
                        listener.update(geoDistDataKey, old, d, toLeg);
                }

                if (mediator != null) {
                    if (predicate == null || predicate.test(toLeg))
                        mediator.update(toLeg, geoDistDataKey, old, d);
                }
            }

            if (fromLeg != null) {
                CachedSegment nextAct = (CachedSegment) fromLeg.next();
                Place place2 = (Place) nextAct.getData(placeDataKey);
                Coordinate c2 = place2.getGeometry().getCoordinate();
                double d = distance(c1, c2);
                //double d = distance(act, nextAct);
                Object old = fromLeg.getData(geoDistDataKey);
                fromLeg.setData(geoDistDataKey, d);

                if (listener != null) {
                    if (predicate == null || predicate.test(fromLeg))
                        listener.update(geoDistDataKey, old, d, fromLeg);
                }

                if (mediator != null) {
                    if (predicate == null || predicate.test(fromLeg))
                        mediator.update(fromLeg, geoDistDataKey, old, d);
                }
            }
        }
    }

    private double distance(Coordinate c1, Coordinate c2) {
        double dx = c1.x - c2.x;
        double dy = c1.y - c2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
//    private double distance(CachedSegment from, CachedSegment to) {
//        Place place1 = (Place) from.getData(placeDataKey);
//        Place place2 = (Place) to.getData(placeDataKey);
//
//        Point point1 = (Point) place1.getGeometry();
//        Point point2 = (Point) place2.getGeometry();
//
//        double dx = point1.getX() - point2.getX();
//        double dy = point1.getY() - point2.getY();
//        return Math.sqrt(dx * dx + dy * dy);
//    }
}
