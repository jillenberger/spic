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

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.mid2008.MidAttributes;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class LegOriginHandler implements LegAttributeHandler {

	@Override
	public void handle(Segment leg, Map<String, String> attributes) {
		String origin = attributes.get(VariableNames.LEG_ORIGIN);

		if(origin.equalsIgnoreCase("1"))
			leg.setAttribute(MidAttributes.KEY.ORIGIN, ActivityTypes.HOME);
		else if(origin.equalsIgnoreCase("2"))
			leg.setAttribute(MidAttributes.KEY.ORIGIN, ActivityTypes.WORK);
		else if(origin.equalsIgnoreCase("3"))
			leg.setAttribute(MidAttributes.KEY.ORIGIN, MidAttributes.DESTINATION.IN_TOWN);
		else if(origin.equalsIgnoreCase("4"))
			leg.setAttribute(MidAttributes.KEY.ORIGIN, MidAttributes.DESTINATION.OUT_OF_TOWN);

	}
}
