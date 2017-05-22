/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
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

package de.dbanalytics.spic.mid2008HH.run;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.mid2008HH.HomeLocator;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

/**
 * Created by johannesillenberger on 08.05.17.
 */
public class RunHomeLocator {

    private static final String MODULE_NAME = "homelocator";

    private static final String TEMPLATES = "templates";

    private static final String ZONES = "zones";

    private static final String PLACES = "places";

    private static final String OUTPUT = "output";

    private static final String PARTITION_KEY = "partitionKey";

    private static final String EPSG_CODE = "epsg";

    private static final String FRACTION = "fraction";

    private static final Logger logger = Logger.getLogger(RunHomeLocator.class);

    public static void main(String args[]) throws IOException, XMLStreamException {
        Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);

        ConfigGroup module = config.getModule(MODULE_NAME);

        logger.info("Loading persons...");
        Set<Person> refPersons = PopulationIO.loadFromXML(module.getValue(TEMPLATES), new PlainFactory());
        logger.info("Loading zones...");
        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(module.getValue(ZONES), "id", null);
        logger.info("Loading places...");
        PlacesIO placesIO = new PlacesIO();
        placesIO.setGeoTransformer(GeoTransformer.WGS84toX(Integer.parseInt(module.getValue(EPSG_CODE))));
        Set<Place> places = placesIO.read(module.getValue(PLACES));

        XORShiftRandom random = new XORShiftRandom(config.global().getRandomSeed());
        HomeLocator locator = new HomeLocator(new PlaceIndex(places), zones, random);
        Set<Person> clones = locator.run(
                refPersons,
                module.getValue(PARTITION_KEY),
                Double.parseDouble(module.getValue(FRACTION)));
        logger.info(String.format("Generated %s persons.", clones.size()));

        logger.info("Writing persons...");
        PopulationIO.writeToXML(module.getValue(OUTPUT), clones);
        logger.info("Done.");
    }
}
