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

import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.StratifiedDiscretizerBuilder;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneGeoJsonIO;
import de.dbanalytics.spic.matrix.GeoDistanceTask;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;

import java.io.IOException;

/**
 * @author johannes
 */
public class Analyzer {

    public static void main(String args[]) throws IOException {
        String matrixFile = "/Users/johannes/gsv/fpd/telefonica/032016/data/plz5.rail.6week.txt";
        String zonesFile = "/Users/johannes/gsv/gis/zones/geojson/plz5.gk3.geojson";
        String outDir = "/Users/johannes/gsv/fpd/telefonica/032016/analysis";

        NumericMatrix m = NumericMatrixIO.read(matrixFile);
        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zonesFile, "plz", "plz5");
        FileIOContext ioContext = new FileIOContext(outDir);

        GeoDistanceTask task = new GeoDistanceTask(
                zones,
                ioContext,
                new StratifiedDiscretizerBuilder(100, 1),
                CartesianDistanceCalculator.getInstance());

        task.analyze(m, null);

    }
}
