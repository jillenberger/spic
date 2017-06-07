/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
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

package de.dbanalytics.spic.mid2008HH.sim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.mid2008.MiDValues;
import de.dbanalytics.spic.sim.AttributeChangeListener;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.CachedSegment;
import de.dbanalytics.spic.sim.data.Converters;
import gnu.trove.map.TObjectIntMap;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

import java.util.Collection;

/**
 * Created by johannesillenberger on 07.06.17.
 */
public class DestinationHamiltonian implements Hamiltonian, AttributeChangeListener {

    private final Object destinationObjectKey = new Object();
    private Object facDataKey;
    private TObjectIntMap<ActivityFacility> zoneIdMapping;
    private int hamiltonian;

    private ActivityFacilities facilities;

    private void initHamiltonian(Collection<CachedPerson> persons) {
        hamiltonian = 0;

        for (CachedPerson person : persons) {
            for (Episode episode : person.getEpisodes()) {
                for (Segment leg : episode.getLegs()) {
                    String value = leg.getAttribute(MiDKeys.LEG_DESTINATION);
                    Integer flag = null;
                    if (MiDValues.IN_TOWN.equalsIgnoreCase(value)) {
                        flag = new Integer(0);
                    } else if (MiDValues.OUT_OF_TOWN.equalsIgnoreCase(value)) {
                        flag = new Integer(1);
                    }

                    if (flag != null) {
                        Segment fromAct = leg.previous();
                        Segment toAct = leg.next();

//                        ActivityFacility fromFac = facilities
                    }
                }
            }
        }
    }

    @Override
    public void onChange(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (facDataKey == null) facDataKey = Converters.getObjectKey(CommonKeys.ACTIVITY_FACILITY);

        if (facDataKey.equals(dataKey)) {
            CachedSegment act = (CachedSegment) element;
            ActivityFacility oldFac = (ActivityFacility) oldValue;
            ActivityFacility newFac = (ActivityFacility) newValue;

            int oldZoneId = zoneIdMapping.get(oldFac);
            int newZoneId = zoneIdMapping.get(newFac);

            if (oldZoneId != newZoneId) {
                CachedSegment toLeg = (CachedSegment) act.previous();
                CachedSegment fromLeg = (CachedSegment) act.next();

                int oldError = 0;
                int newError = 0;

                if (fromLeg != null) {
                    Integer flag = (Integer) fromLeg.getData(destinationObjectKey);
                    if (flag != null) {
                        CachedSegment from = (CachedSegment) toLeg.previous();
                        ActivityFacility facFrom = (ActivityFacility) from.getData(facDataKey);
                        int fromZoneId = zoneIdMapping.get(facFrom);

                        oldError += calcError(fromZoneId, oldZoneId, flag);
                        newError += calcError(fromZoneId, newZoneId, flag);
                    }
                }

                if (toLeg != null) {
                    Integer flag = (Integer) toLeg.getData(destinationObjectKey);
                    if (flag != null) {
                        CachedSegment to = (CachedSegment) fromLeg.next();
                        ActivityFacility facTo = (ActivityFacility) to.getData(facDataKey);
                        int toZoneId = zoneIdMapping.get(facTo);

                        oldError += calcError(oldZoneId, toZoneId, flag);
                        newError += calcError(newZoneId, toZoneId, flag);
                    }
                }

                hamiltonian += (newError - oldError);
            }
        }
    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        return hamiltonian;
    }

    private int calcError(int from, int to, int expected) {
        int result = 1;
        if (from == to) result = 0;
        return Math.abs(expected - result);
    }
}
