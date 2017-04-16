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

package de.dbanalytics.spic.osm;/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 */
public class OSMObject {

    public static final String AREA = "area";

    public static final String BUILDING = "building";

    public static final String POI = "poi";

    private String id;

    private String objectType;

    private String facilityType;

    private Geometry geometry;

    private Set<String> activityOptions = new HashSet<String>();

    public OSMObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    /**
     * @return the type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * @param type the type to set
     */
    public void setObjectType(String type) {
        this.objectType = type;
    }

    /**
     * @return the type
     */
    public String getFacilityType() {
        return facilityType;
    }

    /**
     * @param type the type to set
     */
    public void setFacilityType(String type) {
        this.facilityType = type;
    }

    /**
     * @return the geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * @param geometry the geometry to set
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * @return the activityOptions
     */
    public Set<String> getActivityOptions() {
        return activityOptions;
    }

    public void addActivityOption(String option) {
        activityOptions.add(option);
    }
}
