/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,       *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
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
