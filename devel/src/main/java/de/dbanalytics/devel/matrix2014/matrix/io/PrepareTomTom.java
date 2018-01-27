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

package de.dbanalytics.devel.matrix2014.matrix.io;

import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.matrix.MatrixOperations;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author johannes
 */
public class PrepareTomTom {

    private static final Logger logger = Logger.getLogger(PrepareTomTom.class);

    public static void main(String args[]) throws IOException {
        String inFile = "/home/johannes/gsv/miv-matrix/raw/TomTom/TTgrob_gesamt_aus_zeitunabhängig.txt";
        String outFile = "/home/johannes/gsv/matrix2014/sim/data/matrices/tomtom.de.txt";
        String zonesFile = "/home/johannes/gsv/gis/zones/geojson/tomtom.gk3.geojson";
        String primaryKey = "NO";

        logger.info("Loading matrix...");
        NumericMatrix m = new NumericMatrix();
        VisumOMatrixReader.read(m, inFile);

        logger.info("Loading zones...");
//        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zonesFile, primaryKey, null);
        ZoneIndex zones = null;

        logger.info("Extracting DE matrix...");
        ZoneAttributePredicate p = new ZoneAttributePredicate("NUTS0_CODE", "DE", zones);
        m = (NumericMatrix) MatrixOperations.subMatrix(p, m, new NumericMatrix());

        logger.info("Writing matrix...");
        NumericMatrixIO.write(m, outFile);
        logger.info("Done.");
    }

}
