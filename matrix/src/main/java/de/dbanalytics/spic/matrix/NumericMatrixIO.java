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
package de.dbanalytics.spic.matrix;

import java.io.IOException;

/**
 * @author jillenberger
 */
public class NumericMatrixIO {

    public static NumericMatrix read(String file) {
        if(file.endsWith(".xml") || file.endsWith(".xml.gz")) {
            NumericMatrixXMLReader reader = new NumericMatrixXMLReader();
            reader.setValidating(false);
            reader.parse(file);
            return reader.getMatrix();
        } else {
            try {
                NumericMatrix m = new NumericMatrix();
                NumericMatrixTxtIO.read(m, file);
                return m;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static void write(NumericMatrix m, String file) throws IOException {
        if(file.endsWith(".xml") || file.endsWith(".xml.gz")) {
            NumericMatrixXMLWriter writer = new NumericMatrixXMLWriter();
            writer.write(m, file);
        } else {
            NumericMatrixTxtIO.write(m, file);
        }
    }
}
