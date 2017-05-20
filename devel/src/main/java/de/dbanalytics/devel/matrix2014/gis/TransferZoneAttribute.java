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

import com.vividsolutions.jts.index.strtree.STRtree;
import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneGeoJsonIO;
import de.dbanalytics.spic.mid2008.MiDKeys;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * @author johannes
 */
public class TransferZoneAttribute {

    private static final Logger logger = Logger.getLogger(TransferZoneAttribute.class);

    public static void main(String args[]) throws IOException {
        ZoneCollection source = ZoneGeoJsonIO.readFromGeoJSON("/home/johannes/gsv/gis/zones/geojson/de.lau2.gk3.geojson", "ID", null);
        ZoneCollection target = ZoneGeoJsonIO.readFromGeoJSON("/home/johannes/gsv/gis/zones/geojson/modena.de.gk3.geojson", "NO", null);
        new ZoneSetLAU2Class().apply(source);
        new TransferZoneAttribute().apply(source, target, MiDKeys.PERSON_LAU2_CLASS);
        String data = ZoneGeoJsonIO.toJson(target.getZones());
        Files.write(Paths.get("/home/johannes/gsv/matrix2014/gis/modena.geojson"), data.getBytes(), StandardOpenOption
                .CREATE);

    }

    public void apply(ZoneCollection source, ZoneCollection target, String attribute) {
        /*
        Put all source zones in a spatial index.
         */
        STRtree spatialIndex = new STRtree();
        for(Zone zone : source.getZones()) {
            spatialIndex.insert(zone.getGeometry().getEnvelopeInternal(), zone);
        }
        /*
        Go through all target zones...
         */
        for(Zone zone : target.getZones()) {
            /*
            ... and get all source zones that intersect the target zone.
             */
            List<Zone> candidates = spatialIndex.query(zone.getGeometry().getEnvelopeInternal());
            List<Zone> intersect = new ArrayList<>(candidates.size());

            for(Zone candidate : candidates) {
                if(zone.getGeometry().intersects(candidate.getGeometry())) {
                    intersect.add(candidate);
                }
            }
            /*
            Transfer the attribute...
             */
            if(intersect.size() == 1) {
                /*
                ... only one intersecting source zone found (target is smaller or equal to source)
                 */
                zone.setAttribute(attribute, intersect.get(0).getAttribute(attribute));
            } else if(intersect.size() > 1) {
                /*
                ... multiple intersecting target zones found (target is larger than source). Get the greatest value
                (natural order) of those zones that "significantly" overlap.
                 */
                TreeSet<String> values = new TreeSet<>();
                for(Zone candidate : intersect) {
                    if(candidate.getGeometry().contains(zone.getGeometry().getCentroid()) ||
                            zone.getGeometry().contains(candidate.getGeometry().getCentroid())) {
                        values.add(candidate.getAttribute(attribute));
                    }
                }

                String value = null;
                if(values.isEmpty()) {
                    value = candidates.get(0).getAttribute(attribute);
                    logger.warn("No significant zone overlap. Using attribute of first candidate.");
                } else {
                    value = values.last();
                }
                zone.setAttribute(attribute, value);
            } else {
                logger.info("No intersection between source and target zone.");
            }
        }
    }
}
