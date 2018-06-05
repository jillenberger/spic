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

package de.dbanalytics.spic.mid2008.generator;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Segment;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class LegModeHandler implements LegAttributeHandler {

	@Override
	public void handle(Segment leg, Map<String, String> attributes) {
		String mode = attributes.get(VariableNames.LEG_MODE);
		if(mode.equalsIgnoreCase("1")) {
			leg.setAttribute(Attributes.KEY.MODE, Attributes.MODE.WALK);
		} else if(mode.equalsIgnoreCase("2")) {
			leg.setAttribute(Attributes.KEY.MODE, Attributes.MODE.BIKE);
		} else if(mode.equalsIgnoreCase("3")) {
			leg.setAttribute(Attributes.KEY.MODE, Attributes.MODE.RIDE);
		} else if(mode.equalsIgnoreCase("4")) {
			leg.setAttribute(Attributes.KEY.MODE, Attributes.MODE.CAR);
		} else if(mode.equalsIgnoreCase("5")) {
			leg.setAttribute(Attributes.KEY.MODE, Attributes.MODE.PT);
		}

	}

}
