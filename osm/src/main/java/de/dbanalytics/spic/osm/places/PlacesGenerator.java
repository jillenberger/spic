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

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by johannesillenberger on 25.04.17.
 */
public class PlacesGenerator {

    private static final Logger logger = Logger.getLogger(PlacesGenerator.class);

    public static void main(String args[]) throws IOException {
        String osmFile = "/home/johannesillenberger/prosim-sge0/sge/prj/drive/osm/runs/287/output/places.pbf";
        String tag2placeType = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21 DRIVE/97_Work/osm/Tag2PlaceType.csv";
        InputStream stream = new FileInputStream(osmFile);
        OsmIterator osmIt = null;
        if (osmFile.endsWith(".osm")) osmIt = new OsmXmlIterator(stream, false);
        else if (osmFile.endsWith(".pbf")) osmIt = new PbfIterator(stream, false);

        OsmFeatureBuilder builder = new OsmFeatureBuilder(tag2placeType);
        builder.buildFeatures(osmIt);
    }
}
