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
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * @author johannes
 */
public class RunPlaceStatistics {

    public static final Logger logger = Logger.getLogger(RunPlaceStatistics.class);

    public static void main(String args[]) throws IOException, XMLStreamException {
        logger.info("Loading places...");
        PlacesIO placesIO = new PlacesIO();
        Set<Place> places = placesIO.read(args[0]);
        logger.info(String.format("Loaded %s places.", places.size()));


        TObjectIntMap<String> activities = new TObjectIntHashMap<>();
        TObjectIntMap<String> types = new TObjectIntHashMap<>();

        places.stream().forEach(place -> {
            types.adjustOrPutValue(place.getAttribute("type"), 1, 1);
            place.getActivities().stream().forEach(activity -> activities.adjustOrPutValue(activity, 1, 1));
        });

        logger.info("Types:");
        types.forEachEntry(new TObjectIntProcedure<String>() {
            @Override
            public boolean execute(String a, int b) {
                logger.info(String.format(Locale.US, "%s: %s (%.2f)", a, b, b / (double) places.size() * 100));
                return true;
            }
        });

        logger.info("Activities:");
        activities.forEachEntry(new TObjectIntProcedure<String>() {
            @Override
            public boolean execute(String a, int b) {
                logger.info(String.format(Locale.US, "%s: %s (%.2f)", a, b, b / (double) places.size() * 100));
                return true;
            }
        });
    }
}
