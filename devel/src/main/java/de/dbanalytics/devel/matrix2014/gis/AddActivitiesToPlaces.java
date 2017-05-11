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

package de.dbanalytics.devel.matrix2014.gis;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlacesIO;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author jillenberger
 */
public class AddActivitiesToPlaces {

    public static void main(String args[]) throws IOException, XMLStreamException {
        PlacesIO reader = new PlacesIO();
        Set<Place> places = reader.read("/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/osm/places-raw.xml");
        Map<String, List<String>> activityMapping = loadTypeMapping("/home/johannesillenberger/prosim-sge0/sge/prj/drive/demand/data/Type2Facility.txt");

        for (Place place : places) {
            List<String> activities = activityMapping.get(place.getAttribute("type"));
            if (activities != null) {
                for (String activity : activities) place.addActivity(activity);
            }
            /**
             * FIXME: Each place has an misc activity.
             */
            place.addActivity(ActivityTypes.MISC);
        }

        reader.write(places, "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/osm/places.xml.gz");
    }

    public static Map<String, List<String>> loadTypeMapping(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();

        Map<String, List<String>> mapping = new HashMap<>();

        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\\s");
            String type = tokens[0];
            String facility = tokens[1];

            List<String> types = mapping.get(facility);
            if (types == null) {
                types = new ArrayList<>();
                mapping.put(facility, types);
            }
            types.add(type);
        }

        return mapping;
    }
}
