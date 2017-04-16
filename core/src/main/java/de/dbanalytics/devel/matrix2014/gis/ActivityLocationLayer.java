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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author johannes
 */
public class ActivityLocationLayer {

    public static final String ACTIVITY_TYPE = "activity_type";

    private final Map<String, Feature> locations;

    public ActivityLocationLayer(ActivityFacilities facilities) {
        locations = new LinkedHashMap<>();
        GeometryFactory factory = new GeometryFactory();
        for(ActivityFacility f : facilities.getFacilities().values()) {
            Geometry point = factory.createPoint(new Coordinate(f.getCoord().getX(), f.getCoord().getY()));
            Feature location = new Feature(f.getId().toString(), point);
            locations.put(location.getId(), location);
        }
    }

    public Feature get(String id) {
        return locations.get(id);
    }


}
