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

package de.dbanalytics.spic.source.mid2008HH;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Set;

/**
 * Created by johannesillenberger on 08.05.17.
 */
public class RunHomeLocator {

    public static void main(String args[]) throws IOException, XMLStreamException {
        String refPersonsFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/demand/midHH/mid2008HH2.xml";
        String zoneFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/shapes/Plz8.midHH.geojson";
        String placesFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/osm/places.xml.gz";
        String outFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/demand/midHH/pop.xml.gz";

        String zoneId = "PLZ8";
        String attrKey = "SB_HVV";
        String popKey = "A_GESAMT";

        Set<Person> refPersons = PopulationIO.loadFromXML(refPersonsFile, new PlainFactory());
        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zoneFile, zoneId, null);
        PlacesIO placesIO = new PlacesIO();
        placesIO.setGeoTransformer(GeoTransformer.WGS84toX(31467));
        Set<Place> places = placesIO.read(placesFile);

        HomeLocator locator = new HomeLocator(new PlaceIndex(places), zones);
        locator.setInhabitantsKey(popKey);
        locator.setPartitionKey(attrKey);
        Set<Person> clones = locator.run(refPersons, 0.5);

        PopulationIO.writeToXML(outFile, clones);
    }
}
