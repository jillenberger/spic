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
import de.dbanalytics.spic.sim.AttributeChangeListener;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;

import java.util.Collection;

/**
 * Created by johannesillenberger on 24.05.17.
 */
public class TargetDistance implements Hamiltonian, AttributeChangeListener {

    public static final String TARGET_GEO_DISTANCE = "target_geo_distance";

    private static final double NO_REF_VALUE_ERROR = 1e6;
    private final Object targetDistanceDataKey;
    private Object dataKey;
    private double hamiltonian;

    private boolean isInitialized = false;

    private int legCount;

    public TargetDistance() {
        targetDistanceDataKey = Converters.register(TARGET_GEO_DISTANCE, DoubleConverter.getInstance());
    }

    private void initHamiltonian(Collection<CachedPerson> persons) {
        hamiltonian = 0;
        legCount = 0;
        for (CachedPerson person : persons) {
            for (Episode episode : person.getEpisodes()) {
                for (Segment leg : episode.getLegs()) {
                    double distance = Double.parseDouble(leg.getAttribute(CommonKeys.LEG_GEO_DISTANCE));
                    double targetDistance = Double.parseDouble(leg.getAttribute(TARGET_GEO_DISTANCE));
                    hamiltonian += calculateError(distance, targetDistance);
                    legCount++;
                }
            }
        }

        isInitialized = true;
    }

    @Override
    public void onChange(Object dataKey, Object oldValue, Object newValue, CachedElement element) {
        if (this.dataKey == null) this.dataKey = Converters.getObjectKey(CommonKeys.LEG_GEO_DISTANCE);

        if (dataKey.equals(this.dataKey)) {
            Double targetDistance = (Double) element.getData(targetDistanceDataKey);

            double errOld = calculateError((Double) oldValue, targetDistance);
            double errNew = calculateError((Double) newValue, targetDistance);

            hamiltonian += (errNew - errOld);
        }
    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        if (!isInitialized) initHamiltonian(population);

        return hamiltonian / (double) legCount;
    }

    private double calculateError(double distance, double targetDistance) {
        if (targetDistance == 0) {
            if (distance == 0) return 0;
            else return NO_REF_VALUE_ERROR;
        } else {
            return Math.abs(distance - targetDistance) / targetDistance;
        }

    }
}
