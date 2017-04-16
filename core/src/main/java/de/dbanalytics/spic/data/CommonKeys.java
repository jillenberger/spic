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

package de.dbanalytics.spic.data;

/**
 * @author johannes
 *
 */
public interface CommonKeys {
	
	String PERSON_WEIGHT = "weight"; //TODO: rename to "weight" only

	String LEG_START_TIME = "startTime";
	
	String LEG_END_TIME = "endTime";
	
	String LEG_PURPOSE = "purpose";

	@Deprecated
	String LEG_ROUNDTRIP = "roundTrip";
	
	String LEG_ROUTE_DISTANCE = "routeDistance";

	String LEG_GEO_DISTANCE = "geoDistance";

	String LEG_MODE = "mode";

	String LEG_ROUTE = "route";

	String ACTIVITY_TYPE = "type";
	
	String ACTIVITY_START_TIME = "startTime";
	
	String ACTIVITY_END_TIME = "endTime";
	
	String ACTIVITY_FACILITY = "activityFacility";
	
	String DELETE = "delete";
	
	String DAY = "day";

	String DATA_SOURCE = "datasource";
	
	String HH_INCOME = "hhincome";
	
	String HH_MEMBERS = "hhmembers";
	
	String PERSON_AGE = "age";

	String PERSON_SEX = "sex";

	String PERSON_CARAVAIL = "caravail";

}
