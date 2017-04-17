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

package de.dbanalytics.devel.matrix2014.matrix.postprocess;

import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.matrix.NumericMatrix;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.stats.Discretizer;

import java.util.Map;

/**
 * @author johannes
 */
public class DistanceDimensionCalculator implements DimensionCalculator {

    private NumericMatrix distanceMatrix;

    private Discretizer discretizer;

    private DistanceCalculator calculator;

    private ZoneCollection zones;

    public DistanceDimensionCalculator(ZoneCollection zones, DistanceCalculator calculator, Discretizer discretizer) {
        this.zones = zones;
        this.calculator = calculator;
        this.discretizer = discretizer;
        distanceMatrix = new NumericMatrix();
    }

    @Override
    public String calculate(String origin, String destination, double volume, Map<String, String> dimensions) {
        Double d = distanceMatrix.get(origin, destination);
        if(d == null) {
            Point p_i = zones.get(origin).getGeometry().getCentroid();
            Point p_j = zones.get(destination).getGeometry().getCentroid();

            d = calculator.distance(p_i, p_j);

            distanceMatrix.add(origin, destination, d);
            distanceMatrix.add(destination, origin, d);
        }

        double d2 = discretizer.discretize(d);

        if(d2 == (long) d2)
            return String.format("%d", (long)d2);
        else
            return String.format("%s", d2);
    }
}
