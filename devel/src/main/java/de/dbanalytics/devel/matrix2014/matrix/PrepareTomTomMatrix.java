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

import de.dbanalytics.devel.matrix2014.gis.Zone;
import de.dbanalytics.devel.matrix2014.gis.ZoneCollection;
import de.dbanalytics.devel.matrix2014.gis.ZoneGeoJsonIO;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.gis.DistanceCalculator;

import java.io.IOException;
import java.util.Set;

/**
 * @author jillenberger
 */
public class PrepareTomTomMatrix {

    public static void main(String args[]) throws IOException {
        String in = "/Users/jillenberger/work/matrix2014/data/matrices/tomtom.de.txt";
        String out = "/Users/jillenberger/work/matrix2014/data/matrices/tomtom.de.100KM.txt";
        String zoneFile = "/Users/jillenberger/work/matrix2014/data/zones/nuts3.psm.gk3.geojson";
        String idKey = "NO";

        NumericMatrix m = NumericMatrixIO.read(in);
        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zoneFile, idKey, null);
        DistanceCalculator dCalc = CartesianDistanceCalculator.getInstance();
        double threshold = 100000;

        Set<String> keys = m.keys();
        for(String i : keys) {
            Zone z_i = zones.get(i);
            for(String j : keys) {
                Zone z_j = zones.get(j);

                double d = dCalc.distance(z_i.getGeometry().getCentroid(), z_j.getGeometry().getCentroid());
                if(d < threshold) {
                    m.set(i, j, null);
                }
            }
        }

        NumericMatrixIO.write(m, out);
    }
}
