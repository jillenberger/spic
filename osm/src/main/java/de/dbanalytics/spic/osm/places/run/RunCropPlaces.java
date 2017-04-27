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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.osm.places.run;


import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneGeoJsonIO;

import java.io.*;

/**
 * Created by johannesillenberger on 27.04.17.
 */
public class RunCropPlaces {

    public static final void main(String args[]) throws IOException {
        String inFile = args[0];
        String outFile = args[1];
        String shapeFile = args[2];

        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(shapeFile, "PLZ8", null);

        BufferedReader reader = new BufferedReader(new FileReader(inFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        String line = reader.readLine();
        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\t");
            double lon = Double.parseDouble(tokens[0]);
            double lat = Double.parseDouble(tokens[1]);

            Zone zone = zones.get(new Coordinate(lon, lat));
            if (zone != null) {
                writer.write(line);
                writer.newLine();
            }
        }

        writer.close();
        reader.close();
    }
}
