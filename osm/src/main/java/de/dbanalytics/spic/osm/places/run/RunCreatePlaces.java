/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.osm.places.run;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlacesIO;
import org.geotools.geometry.jts.JTSFactoryFinder;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jillenberger
 */
public class RunCreatePlaces {

    public static void main(String args[]) throws IOException, XMLStreamException {
        String placesTxtFile = args[0];
        String placesXmlFile = args[1];

        BufferedReader reader = new BufferedReader(new FileReader(placesTxtFile));
        String line = reader.readLine();
        int cnt = 1;
        GeometryFactory factory = JTSFactoryFinder.getGeometryFactory();
        Set<Place> places = new HashSet<>();

        while ((line = reader.readLine()) != null) {
            String tokens[] = line.split("\\s");
            double lon = Double.parseDouble(tokens[0]);
            double lat = Double.parseDouble(tokens[1]);
            String type = tokens[2];

            Place place = new Place(String.valueOf(cnt++), factory.createPoint(new Coordinate(lon, lat)));
            place.setAttribute("type", type);

            places.add(place);
        }

        PlacesIO writer = new PlacesIO();
        writer.write(places, placesXmlFile);
    }
}
