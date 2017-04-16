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
public class LegDestinationLocHandler implements LegAttributeHandler {

	@Override
	public void handle(Attributable leg, String key, String value) {
		if(key.endsWith("ziel0")) {
			if(value.equals("1")) {
				leg.setAttribute(InvermoKeys.DESTINATION_LOCATION, "home");
			} else if(value.equals("2")){
				leg.setAttribute(InvermoKeys.DESTINATION_LOCATION, "work");
			}
		} else if(key.endsWith("zielland") || key.endsWith("zieldort") || key.endsWith("ziela3")) {
			String desc = leg.getAttribute(InvermoKeys.DESTINATION_LOCATION);
			if(desc == null) {
				desc = value;
			} else {
				desc = desc + ", " + value;
			}
			leg.setAttribute(InvermoKeys.DESTINATION_LOCATION, desc);
		}

	}

}
