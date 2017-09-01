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

import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlacesIO;
import de.dbanalytics.spic.util.ProgressLogger;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author johannes
 */
public class RunSubsetPlaces {

    private static final Logger logger = Logger.getLogger(RunSubsetPlaces.class);

    public static void main(String args[]) throws IOException, XMLStreamException {
        String inFile = args[0];
        String outFile = args[1];
        double p = Double.parseDouble(args[2]);

        String type = null;
        if (args.length >= 4) type = args[3];

        Random random = new XORShiftRandom();

        logger.info("Loading places...");
        PlacesIO placesIO = new PlacesIO();
        Set<Place> places = placesIO.read(inFile);

        ProgressLogger plogger = new ProgressLogger(logger);
        plogger.start("Subsetting places...", places.size());
        Set<Place> newPlaces = new HashSet<>(places.size());
        for (Place place : places) {
            if (type == null) {
                if (random.nextDouble() <= p) {
                    newPlaces.add(place);
                }
            } else {
                String ptype = place.getAttribute("type");
                if (type.equalsIgnoreCase(ptype)) {
                    if (random.nextDouble() <= p) {
                        newPlaces.add(place);
                    }
                } else {
                    newPlaces.add(place);
                }
            }
            plogger.step();
        }
        plogger.stop();

        logger.info(String.format("Removed %s of %s places (%.2f %%).",
                places.size() - newPlaces.size(),
                places.size(),
                (places.size() - newPlaces.size()) / (double) places.size() * 100));

        logger.info("Writing places...");
        placesIO.write(newPlaces, outFile);
        logger.info("Done.");
    }
}
