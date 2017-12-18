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


import de.dbanalytics.spic.gis.*;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Created by johannesillenberger on 27.04.17.
 */
public class RunCropPlaces {

    private static final Logger logger = Logger.getLogger(RunCropPlaces.class);

    public static final void main(String args[]) throws IOException, XMLStreamException {
        String inFile = args[0];
        String outFile = args[1];
        String shapeFile = args[2];

        GeoTransformer transformer = null;
        if (args.length > 3) {
            int srid = Integer.parseInt(args[3]);
            transformer = GeoTransformer.WGS84toX(srid);
        }

        logger.info("Loading places...");
        PlacesIO placesIO = new PlacesIO();
        if (transformer != null) placesIO.setGeoTransformer(transformer);
        Set<Place> places = placesIO.read(inFile);

        logger.info("Loading features...");
        FeaturesIO featuresIO = new FeaturesIO();
        if (transformer != null) featuresIO.setTransformer(transformer);
        Set<Feature> features = featuresIO.read(shapeFile);
        ZoneIndex zoneIndex = new ZoneIndex(features);

        logger.info("Cropping...");
        List<Place> subset = new ArrayList<>(places.size());
        for (Place place : places) {
            Feature feature = zoneIndex.get(place.getGeometry().getCoordinate());
            if (feature != null) subset.add(place);
        }
        logger.info(String.format(Locale.US, "Removed %s of %s places (%.2f %%).",
                places.size() - subset.size(),
                places.size(),
                (places.size() - subset.size()) / (double) places.size() * 100));
        logger.info("Writing places...");
        placesIO.write(subset, outFile);
        logger.info("Done.");
    }
}
