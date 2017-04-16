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
package de.dbanalytics.devel.matrix2014.matrix;

import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.matrix.Matrix;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.gis.OrthodromicDistanceCalculator;

/**
 * @author jillenberger
 */
public class ZoneDistancePredicate implements ODPredicate<String, Double> {

    private final ZoneCollection zones;

    private final DistanceCalculator calculator;

    private final double threshold;

    public ZoneDistancePredicate(ZoneCollection zones, double threshold) {
        this(zones, threshold, new OrthodromicDistanceCalculator());
    }

    public ZoneDistancePredicate(ZoneCollection zones, double threshold, DistanceCalculator calculator) {
        this.zones = zones;
        this.threshold = threshold;
        this.calculator = calculator;
    }

    @Override
    public boolean test(String row, String col, Matrix<String, Double> matrix) {
        Point p_i = zones.get(row).getGeometry().getCentroid();
        Point p_j = zones.get(col).getGeometry().getCentroid();
        double d = calculator.distance(p_i, p_j);

        return (d >= threshold);
    }
}
