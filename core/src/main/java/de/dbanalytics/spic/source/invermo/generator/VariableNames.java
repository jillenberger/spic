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

package de.dbanalytics.spic.source.invermo.generator;

/**
 * @author johannes
 *
 */
public class VariableNames {

	public static final String HOUSEHOLD_ID = "ID";
	
	public static final String PERSON_ID = "persnr";

	public static final String TRIP_ID = "Reisenr";
	
	public static final String STATION_NAME = "hhbhfname";
	
	public static final String STATION_DIST = "hhbhfkm";
	
	public static final String HOME_TOWN = "wohnort";
	
	public static final String HOME_ZIPCODE = "wohnplz";
	
	public static final String NA = "nan";
	
	public static final String START1_TRIP1 = "e1start1";
	
	public static final String START2_TRIP1 = "e1start2";
	
	public static final String START1_TRIP2 = "e2start1";
	
	public static final String START2_TRIP2 = "e2start2";
	
	public static final String START1_TRIP3 = "e3start1";
	
	public static final String START2_TRIP3 = "e3start2";
	
	public static final String START1_TRIP4 = "e4start1";
	
	public static final String MOB_WEIGTH = "gewmobil";

	public static final String WORK_COUNTRY = "arbland";

	public static final String WORK_TOWN = "arbstadtd";

	public static final String WORK_ZIP = "arbplzd";
	
	public static boolean validate(String value) {
		return (value != null && !value.equalsIgnoreCase(NA) && !value.isEmpty());
	}
}
