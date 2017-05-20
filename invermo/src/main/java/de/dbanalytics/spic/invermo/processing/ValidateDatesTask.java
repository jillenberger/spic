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

package de.dbanalytics.spic.invermo.processing;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.processing.EpisodeTask;

import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 *
 */
public class ValidateDatesTask implements EpisodeTask {

	private final Map<String, String> replacements;
	
	public ValidateDatesTask() {
		replacements = new HashMap<String, String>();
		replacements.put("0", "2000");
		replacements.put("1", "2001");
		replacements.put("2", "2002");
		replacements.put("99", "1999");
		replacements.put("3899", "1999");
		replacements.put("82", "1982");
		replacements.put("98", "1998");
	}

	@Override
	public void apply(Episode plan) {
		for(Attributable leg : plan.getLegs()) {
			String startYear = leg.getAttribute("startTimeYear");
			if(startYear != null)
				leg.setAttribute("startTimeYear", validate(startYear));
			
			String endYear = leg.getAttribute("endTimeYear");
			if(endYear != null)
				leg.setAttribute("endTimeYear", validate(endYear));
		}
	}

	private String validate(String str) {
		String replace = replacements.get(str);
		if(replace == null) {
			return str;
		} else {
			return replace;
		}
	}
}
