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

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.source.mid2008.MiDKeys;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class JourneyDaysHandler implements EpisodeAttributeHandler {

	@Override
	public void handle(Episode plan, Map<String, String> attributes) {
		String value = attributes.get(VariableNames.JOURNEY_NIGHTS);
		if(value != null) {
			int nights = Integer.parseInt(value);

			if (nights < 95)
				plan.setAttribute(MiDKeys.JOURNEY_DAYS, String.valueOf(nights + 1));
		}
	}

}
