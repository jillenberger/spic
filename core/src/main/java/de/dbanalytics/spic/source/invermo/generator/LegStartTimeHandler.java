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

/**
 * @author johannes
 *
 */
public class LegStartTimeHandler implements LegAttributeHandler {

	@Override
	public void handle(Attributable leg, String key, String value) {
		if(key.endsWith("abstd")) {
			leg.setAttribute("startTimeHour", value);
		} else if(key.endsWith("abmin")) {
			leg.setAttribute("startTimeMin", value);
		} else if(key.endsWith("abtag")) {
			leg.setAttribute("startTimeDay", value);
		} else if(key.endsWith("abmonat")) {
			leg.setAttribute("startTimeMonth", value);
		} else if(key.endsWith("abjahr")) {
			leg.setAttribute("startTimeYear", value);
		}
		
	}

}
