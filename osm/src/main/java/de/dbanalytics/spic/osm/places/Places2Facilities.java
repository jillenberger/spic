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

package de.dbanalytics.spic.osm.places;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesUtils;
import org.matsim.facilities.FacilitiesWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by johannesillenberger on 26.04.17.
 */
public class Places2Facilities {

    public static void main(String args[]) throws IOException {
//        GeoTransformer transformer = new GeoTransformer(4326, 31467);
        GeoTransformer transformer = new GeoTransformer(4326, 3857);
        ActivityFacilities facilities = FacilitiesUtils.createActivityFacilities();

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line = reader.readLine();
        int counter = 1;
        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\t");
            double coord[] = new double[2];
            coord[0] = Double.parseDouble(tokens[0]);
            coord[1] = Double.parseDouble(tokens[1]);
            transformer.forward(coord);

            Id<ActivityFacility> id = Id.create(counter++, ActivityFacility.class);
            ActivityFacility facility = facilities.getFactory().createActivityFacility(
                    id, new Coord(coord[0], coord[1]));
            facility.addActivityOption(facilities.getFactory().createActivityOption(tokens[2]));

            facilities.addActivityFacility(facility);
        }

        FacilitiesWriter writer = new FacilitiesWriter(facilities);
        writer.write(args[1]);
    }
}
