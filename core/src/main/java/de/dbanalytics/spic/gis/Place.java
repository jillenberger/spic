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

package de.dbanalytics.spic.gis;

import com.vividsolutions.jts.geom.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jillenberger
 */
public class Place extends Feature {

    private List<String> activities;

    private List<String> immutable;

    public Place(String id, Point geometry) {
        super(id, geometry);
        activities = new ArrayList<>(2);
        immutable = Collections.unmodifiableList(activities);
    }

    public void addActivity(String activity) {
        if (!activities.contains(activity)) activities.add(activity);
    }

    public void removeActivity(String activity) {
        activities.remove(activity);
    }

    public List<String> getActivities() {
        return immutable;
    }

}
