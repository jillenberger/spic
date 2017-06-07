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

package de.dbanalytics.devel.gis;

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneGeoJsonIO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Created by johannesillenberger on 04.05.17.
 */
public class AdoptParentAttribute {

    public static void main(String args[]) throws IOException {
        String childZonesFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/04_Daten/Shapes/PLZ8.midHH.geojson";
        String parentZonesFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/04_Daten/Shapes/lau2.hh.gk3.geojson";
        String outFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/shapes/PLZ8-sim.gk3.geojson";

//        new FeaturesIO().read("/home/johannesillenberger/prosim-sge0/sge/prj/drive/demand/data/zones/PLZ8.midHH.geojson");

        ZoneCollection childZones = ZoneGeoJsonIO.readFromGeoJSON(childZonesFile, "PLZ8", null);
        ZoneCollection parentZones = ZoneGeoJsonIO.readFromGeoJSON(parentZonesFile, "ID", null);


        new AdoptParentAttribute().run(childZones.getZones(), parentZones, "Lau2Class");

        String data = ZoneGeoJsonIO.toJson(childZones.getZones());
        Files.write(Paths.get(outFile), data.getBytes());
    }

    public void run(Collection<Zone> zones, ZoneCollection parentZones, String key) {
        for (Zone zone : zones) {
            Coordinate centroid = zone.getGeometry().getCentroid().getCoordinate();
            Zone parent = parentZones.get(centroid);
            if (parent != null) {
                zone.setAttribute(key, parent.getAttribute(key));
            }
        }
    }
}
