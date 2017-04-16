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

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedSegment;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;
import org.matsim.api.core.v01.Coord;
import org.matsim.facilities.ActivityFacility;

/**
 * @author jillenberger
 */
public class GeoDistanceUpdater implements AttributeChangeListener {

    private Object facDataKey;

    private final Object geoDistDataKey = Converters.register(CommonKeys.LEG_GEO_DISTANCE, DoubleConverter.getInstance());

    private AttributeChangeListener listener;

    private Predicate<CachedSegment> predicate;

    public GeoDistanceUpdater() {
        this.listener = null;
    }

    public GeoDistanceUpdater(AttributeChangeListener listener) {
        setListener(listener);
    }

    public void setPredicate(Predicate<CachedSegment> predicate) {
        this.predicate = predicate;
    }

    public void setListener(AttributeChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onChange(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (facDataKey == null) facDataKey = Converters.getObjectKey(CommonKeys.ACTIVITY_FACILITY);

        if (facDataKey.equals(dataKey)) {
            CachedSegment act = (CachedSegment) element;
            CachedSegment toLeg = (CachedSegment) act.previous();
            CachedSegment fromLeg = (CachedSegment) act.next();

            if (toLeg != null) {
                CachedSegment prevAct = (CachedSegment) toLeg.previous();
                double d = distance(prevAct, act);
                Object old = toLeg.getData(geoDistDataKey);
                toLeg.setData(geoDistDataKey, d);

                if (listener != null) {
                    if (predicate == null || predicate.test(toLeg))
                        listener.onChange(geoDistDataKey, old, d, toLeg);
                }
            }

            if (fromLeg != null) {
                CachedSegment nextAct = (CachedSegment) fromLeg.next();
                double d = distance(act, nextAct);
                Object old = fromLeg.getData(geoDistDataKey);
                fromLeg.setData(geoDistDataKey, d);

                if (listener != null) {
                    if (predicate == null || predicate.test(fromLeg))
                        listener.onChange(geoDistDataKey, old, d, fromLeg);
                }
            }
        }
    }

    private double distance(CachedSegment from, CachedSegment to) {
        ActivityFacility fac1 = (ActivityFacility) from.getData(facDataKey);
        ActivityFacility fac2 = (ActivityFacility) to.getData(facDataKey);

        Coord c1 = fac1.getCoord();
        Coord c2 = fac2.getCoord();

        double dx = c1.getX() - c2.getX();
        double dy = c1.getY() - c2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
