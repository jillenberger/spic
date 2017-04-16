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

package de.dbanalytics.spic.source.mid2008.generator;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;

import java.util.Map;

/**
 * @author johannes
 * 
 */
public class JourneyDistanceHandler implements LegAttributeHandler {

	@Override
	public void handle(Segment leg, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.JOURNEY_DISTANCE);

		if(val != null) {
			int dist = Integer.parseInt(val);

			if (dist <= 20000) { //range according to mid documentation
				dist *= 1000;
				leg.setAttribute(CommonKeys.LEG_GEO_DISTANCE, String.valueOf(dist));
			}
		}
	}

}
