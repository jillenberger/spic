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
package de.dbanalytics.devel.matrix2014.data;

import de.dbanalytics.devel.matrix2014.gis.FacilityData;
import de.dbanalytics.spic.sim.data.Converter;
import org.matsim.api.core.v01.Id;
import org.matsim.facilities.ActivityFacility;

/**
 * @author jillenberger
 */
public class ActivityFacilityConverter implements Converter {

    private static ActivityFacilityConverter instance;

    public static ActivityFacilityConverter getInstance(FacilityData data) {
        if(instance == null) {
            instance = new ActivityFacilityConverter(data);
        }

        return instance;
    }

    private final FacilityData data;

    public ActivityFacilityConverter(FacilityData data) {
        this.data = data;
    }

    @Override
    public Object toObject(String value) {
        return data.getAll().getFacilities().get(Id.create(value, ActivityFacility.class));
    }

    @Override
    public String toString(Object value) {
        return ((ActivityFacility)value).getId().toString();
    }
}
