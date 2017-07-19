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

package de.dbanalytics.spic.spic2matsim;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.gis.GeoTransformer;
import de.dbanalytics.spic.gis.Place;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityOption;
import org.matsim.facilities.FacilitiesUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author jillenberger
 */
public class PlaceConverter {

    private GeoTransformer transformer;

    private GeometryFactory geometryFactory;

    public PlaceConverter() {
        transformer = GeoTransformer.identityTransformer();
    }

    public void setTransformer(GeoTransformer transformer) {
        this.transformer = transformer;
    }

    public ActivityFacility convert(Place place, ActivityFacilities facilities) {
        Id<ActivityFacility> id = Id.create(place.getId(), ActivityFacility.class);
        Coordinate coordinate = place.getGeometry().getCoordinate();
        transformer.forward(coordinate);
        Coord coord = new Coord(coordinate.x, coordinate.y);

        ActivityFacility facility = facilities.getFactory().createActivityFacility(id, coord);

        for (String activity : place.getActivities()) {
            facility.addActivityOption(facilities.getFactory().createActivityOption(activity));
        }

        return facility;
    }

    public ActivityFacilities convert(Collection<Place> places) {
        ActivityFacilities facilities = FacilitiesUtils.createActivityFacilities();
        places.stream().forEach(place -> facilities.addActivityFacility(convert(place, facilities)));
        return facilities;
    }

    public Place convert(ActivityFacility facility) {
        Point point = geometryFactory.createPoint(new Coordinate(facility.getCoord().getX(), facility.getCoord().getY()));
        transformer.forward(point);

        Place place = new Place(facility.getId().toString(), point);
        for (ActivityOption option : facility.getActivityOptions().values()) place.addActivity(option.getType());

        return place;
    }

    public Set<Place> convert(ActivityFacilities facilities) {
        geometryFactory = JTSFactoryFinder.getGeometryFactory();
        Set<Place> places = new HashSet<>(facilities.getFacilities().size());
        for (ActivityFacility facility : facilities.getFacilities().values()) {
            places.add(convert(facility));
        }

        return places;
    }
}
