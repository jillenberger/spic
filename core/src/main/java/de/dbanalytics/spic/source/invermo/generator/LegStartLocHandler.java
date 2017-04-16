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

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.source.invermo.InvermoKeys;

/**
 * @author johannes
 *
 */
public class LegStartLocHandler implements LegAttributeHandler {

	@Override
	public void handle(Attributable leg, String key, String value) {
		if(key.contains(VariableNames.START1_TRIP1)) {
			if(value.equalsIgnoreCase("1")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "home");
			} else if(value.equalsIgnoreCase("2")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "work");
			}
		} else if(key.contains(VariableNames.START2_TRIP1)) {
			if(value.equalsIgnoreCase("1")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "secondHome");
			} else if(key.equalsIgnoreCase("2")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "friends");
			} else if(key.equalsIgnoreCase("3")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "buisiness");
			}
			
		} else if(key.contains(VariableNames.START1_TRIP2) || key.contains(VariableNames.START1_TRIP3)) {
			if(value.equalsIgnoreCase("1")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "prev");
			} else if(value.equalsIgnoreCase("2")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "sameTown");
			}
		} else if(key.contains(VariableNames.START2_TRIP2) || key.contains(VariableNames.START2_TRIP3)) {
			leg.setAttribute(InvermoKeys.START_LOCATION, value);
		} else if(key.contains(VariableNames.START1_TRIP4)) {
			if(value.equals("1")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "prev");
			} else if(value.equals("2")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "other");
			}
		} else if(key.contains("e4start1a")) {
			if(value.equals("1")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "germany");
			} else if(value.equals("2")) {
				leg.setAttribute(InvermoKeys.START_LOCATION, "foreign");
			}
		} else if(key.contains("e4startd1") || key.contains("e4startd2") || key.contains("e4startd3") || key.contains("e4startd4")) {
			String desc = leg.getAttribute(InvermoKeys.START_LOCATION);
			if(desc == null) {
				desc = value;
			} else {
				desc = desc + ", " + value;
			}
			leg.setAttribute(InvermoKeys.START_LOCATION, desc);
		}
	}

}
