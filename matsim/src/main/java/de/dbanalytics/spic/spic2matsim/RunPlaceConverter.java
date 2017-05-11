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

package de.dbanalytics.spic.spic2matsim;

import de.dbanalytics.spic.gis.GeoTransformer;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlacesIO;
import org.apache.log4j.Logger;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.FacilitiesWriter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

/**
 * Created by johannesillenberger on 10.05.17.
 */
public class RunPlaceConverter {

    private static final Logger logger = Logger.getLogger(RunPlaceConverter.class);

    public static void main(String args[]) throws IOException, XMLStreamException {
        String inFile = args[0];
        String outFile = args[1];

        logger.info("Loading places...");
        PlacesIO placesIO = new PlacesIO();
        Set<Place> places = placesIO.read(inFile);
        logger.info(String.format("Loaded %s places.", places.size()));

        logger.info("Converting places...");
        PlaceConverter converter = new PlaceConverter();
        if (args.length > 2) {
            converter.setTransformer(GeoTransformer.WGS84toX(Integer.parseInt(args[2])));
        }
        ActivityFacilities facilities = converter.convert(places);
        logger.info(String.format("Created %s facilities.", facilities.getFacilities().size()));

        logger.info("Writing facilities...");
        FacilitiesWriter writer = new FacilitiesWriter(facilities);
        writer.write(outFile);
        logger.info("Done.");
    }
}
