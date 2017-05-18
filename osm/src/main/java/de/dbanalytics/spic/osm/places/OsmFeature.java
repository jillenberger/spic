/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.osm.places;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by johannesillenberger on 25.04.17.
 */
public class OsmFeature {

    public static final String LANDUSE = "landuse";

    public static final String BUILDING = "building";

    public static final String POI = "poi";


    private final Geometry geometry;

    private final String featureType;

    private Set<String> placeTypes;

    private OsmFeature parent;

    private Set<OsmFeature> children;

    public OsmFeature(Geometry geometry, String featureType) {
        this.geometry = geometry;
        this.featureType = featureType;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public Set<String> getPlaceTypes() {
        return placeTypes;
    }

    public void addPlaceType(String type) {
        if (placeTypes == null) placeTypes = new HashSet<>();
        placeTypes.add(type);
    }

    public void removePlaceType(String type) {
        if (placeTypes != null) placeTypes.remove(type);
    }

    public boolean isValid() {
        return ((placeTypes != null && !placeTypes.isEmpty()) || isBuilding());
    }

    public boolean isBuilding() {
        return BUILDING.equalsIgnoreCase(featureType);
    }

    public boolean isLanduse() {
        return LANDUSE.equalsIgnoreCase(featureType);
    }

    public boolean isPoi() {
        return POI.equalsIgnoreCase(featureType);
    }

    public OsmFeature getParent() {
        return parent;
    }

    public void setParent(OsmFeature feature) {
        parent = feature;
    }

    public Set<OsmFeature> getChildren() {
        return children;
    }

    public void addChild(OsmFeature feature) {
        if (children == null) children = new HashSet<>();
        children.add(feature);
    }

}
