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

package de.dbanalytics.devel.matrix2014.gis;

import de.dbanalytics.spic.gis.*;
import org.matsim.contrib.common.util.ProgressLogger;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jillenberger
 */
public class PlaceIndexTest {

    public static void main(String args[]) throws IOException, XMLStreamException {
        PlacesIO placesIO = new PlacesIO();
        placesIO.setGeoTransformer(GeoTransformer.WGS84toX(31467));

        System.out.println("Loading places...");
        Set<Place> places = placesIO.read("/Users/jillenberger/Desktop/places2.xml.gz");
        PlaceIndex placeIndex = new PlaceIndex(places);

        System.out.println("Loading zones...");
        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON("/Users/jillenberger/Dropbox/work/zones.gk3.geojson", "NO", null);

        Map<String, Integer> counts = new ConcurrentHashMap<>();
        System.out.println("Init...");

        placeIndex.get(zones.getZones().iterator().next().getGeometry());
        System.out.println("Quering...");
        long time = System.currentTimeMillis();
        ProgressLogger.init(zones.getZones().size(), 2, 10);


//        for(Zone zone : zones.getZones()) {
//            Set<Place> result = placeIndex.get(zone.getGeometry());
//            counts.put(zone.getAttribute("NO"), result.size());
//            ProgressLogger.step();
//        }

        zones.getZones().parallelStream().forEach(zone -> {
            Set<Place> result = placeIndex.get(zone.getGeometry());
            counts.put(zone.getAttribute("NO"), result.size());
            ProgressLogger.step();
        });

        ProgressLogger.terminate();
        System.out.println("Time: " + (System.currentTimeMillis() - time));

        SortedMap<String, Integer> sorted = new TreeMap<>(counts);
        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/jillenberger/Desktop/STRtree2.txt"));
        for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
            writer.write(entry.getKey());
            writer.write("\t");
            writer.write(String.valueOf(entry.getValue()));
            writer.newLine();
        }
        writer.close();
    }
}
