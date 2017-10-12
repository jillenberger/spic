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

package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

/**
 * Created by johannesillenberger on 11.04.17.
 */
public class Episodes2Matrix {

    private static final Logger logger = Logger.getLogger(Episodes2Matrix.class);

    private static final String MODULE_NAME = "episodes2matrix";

    private static final String ZONES_FILE_PARAM = "zoneFile";

    private static final String ZONE_KEY_PARAM = "zoneKey";

    private static final String PLACES_FILE_PARAM = "placesFile";

    private static final String PERSONS_FILE_PARAM = "personsFile";

    private static final String MATRIX_FILE_PARAM = "matrixFile";

    private static final String SRID_PARAM = "srid";

    public static void main(String args[]) throws IOException, XMLStreamException {
        final Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);
        ConfigGroup group = config.getModules().get(MODULE_NAME);

        logger.info("Loading zones...");
        int srid = Integer.parseInt(config.getParam(MODULE_NAME, SRID_PARAM));
        FeaturesIO featuresIO = new FeaturesIO();
        featuresIO.setTransformer(GeoTransformer.WGS84toX(srid));
        Set<Feature> features = featuresIO.read(group.getParams().get(ZONES_FILE_PARAM));
        ZoneIndex zoneIndex = new ZoneIndex(features);

        logger.info("Loading places...");
        PlacesIO placesReader = new PlacesIO();
        placesReader.setGeoTransformer(GeoTransformer.WGS84toX(srid));
        Set<Place> places = placesReader.read(config.getParam(MODULE_NAME, PLACES_FILE_PARAM));

        logger.info("Loading persons...");
        Set<? extends Person> persons = PopulationIO.loadFromXML(group.getParams().get(PERSONS_FILE_PARAM), new PlainFactory());

        logger.info("Building matrix...");
        DefaultMatrixBuilder builder = new DefaultMatrixBuilder(
                new PlaceIndex(places),
                zoneIndex);
        NumericMatrix m = builder.build(persons);

        logger.info("Writing matrix...");
        NumericMatrixIO.write(m, group.getParams().get(MATRIX_FILE_PARAM));
        logger.info("Done.");

        Executor.shutdown();
    }
}
