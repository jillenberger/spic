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

import de.dbanalytics.spic.gis.GeoTransformer;
import de.dbanalytics.spic.osm.places.OsmFeature;
import de.dbanalytics.spic.osm.places.OsmFeatureBuilder;
import de.dbanalytics.spic.osm.places.PlacesSynthesizer;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by johannesillenberger on 26.04.17.
 */
public class RunOsm2Places {

    public static void main(String args[]) throws IOException {
        String osmFile = args[0];
        String placesFile = args[1];
        String tag2placeType = args[2];


        InputStream stream = new FileInputStream(osmFile);
        OsmIterator osmIt = null;
        if (osmFile.endsWith(".osm")) osmIt = new OsmXmlIterator(stream, false);
        else if (osmFile.endsWith(".pbf")) osmIt = new PbfIterator(stream, false);

        OsmFeatureBuilder builder = new OsmFeatureBuilder(tag2placeType);
        Set<OsmFeature> features = builder.buildFeatures(osmIt);

        PlacesSynthesizer synthesizer = new PlacesSynthesizer();
        if (args.length > 3) {
            int epsg = Integer.parseInt(args[3]);
            synthesizer.setGeoTransformer(GeoTransformer.WGS84toX(epsg));
        }
        if (args.length > 4) {
            double area = Double.parseDouble(args[4]);
            synthesizer.setAreaThreshold(area);
        }

        synthesizer.synthesize(features, placesFile);
    }
}
