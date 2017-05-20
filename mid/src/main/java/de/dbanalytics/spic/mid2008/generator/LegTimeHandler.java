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

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class LegTimeHandler implements LegAttributeHandler {

    private static final String NA_VALUE = "301";

	@Override
	public void handle(Segment leg, Map<String, String> attributes) {
        Integer time = calcSeconds(attributes, true);
        if (time != null) leg.setAttribute(CommonKeys.LEG_START_TIME, String.valueOf(time));

		time = calcSeconds(attributes, false);
        if (time != null) leg.setAttribute(CommonKeys.LEG_END_TIME, String.valueOf(time));

	}

    private Integer calcSeconds(Map<String, String> attributes, boolean mode) {
        String hKey = VariableNames.LEG_END_TIME_HOUR;
		String mKey = VariableNames.LEG_END_TIME_MIN;
		String dKey = VariableNames.END_NEXT_DAY;

		if(mode) {
			hKey = VariableNames.LEG_START_TIME_HOUR;
			mKey = VariableNames.LEG_START_TIME_MIN;
			dKey = VariableNames.START_NEXT_DAY;
		}

		String hour = attributes.get(hKey);
		String min = attributes.get(mKey);
		String nextDay = attributes.get(dKey);

        Integer time = null;
        if (hour != null && min != null) {
            if (NA_VALUE.equals(hour)) return null;

            time = Integer.parseInt(min) * 60 + Integer.parseInt(hour) * 60 * 60;

            if (nextDay != null && nextDay.equalsIgnoreCase("1")) {
                time += 86400;
            }
        }

		return time;
	}
}
