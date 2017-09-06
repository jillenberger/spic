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
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.FeaturesIO;
import de.dbanalytics.spic.gis.ZoneIndex;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * Created by johannesillenberger on 04.05.17.
 */
public class AdoptParentAttribute {

    public static void main(String args[]) throws IOException {
        String childZonesFile = args[0];
        String parentZonesFile = args[1];
        String outFile = args[2];

        FeaturesIO featuresIO = new FeaturesIO();
        Set<Feature> childZones = featuresIO.read(childZonesFile);
        Set<Feature> parentZones = featuresIO.read(parentZonesFile);

        new AdoptParentAttribute().run(childZones, new ZoneIndex(parentZones), "lau2class");

        featuresIO.write(childZones, outFile);
    }

    public void run(Collection<Feature> zones, ZoneIndex parentZones, String key) {
        for (Feature zone : zones) {
            Coordinate centroid = zone.getGeometry().getCentroid().getCoordinate();
            Feature parent = parentZones.get(centroid);
            if (parent != null) {
                zone.setAttribute(key, parent.getAttribute(key));
            }
        }
    }
}
