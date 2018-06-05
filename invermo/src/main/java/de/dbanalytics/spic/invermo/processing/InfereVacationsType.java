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

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.processing.EpisodeTask;
import org.joda.time.DateTime;

/**
 * @author johannes
 * 
 */
public class InfereVacationsType implements EpisodeTask {

	@Override
	public void apply(Episode plan) {
		boolean hasVacations = false;
		for (Attributable act : plan.getActivities()) {
			if ("vacations".equalsIgnoreCase(act.getAttribute(Attributes.KEY.TYPE))) {
				hasVacations = true;
				break;
			}
		}

		if (hasVacations) {
			boolean isLong = false;

			Attributable first = plan.getLegs().get(0);
			Attributable last = plan.getLegs().get(plan.getLegs().size() - 1);

			String startStr = first.getAttribute(Attributes.KEY.DEPARTURE_TIME);
			String endStr = last.getAttribute(Attributes.KEY.ARRIVAL_TIME);

			if (startStr != null && endStr != null) {
				DateTime start = SplitPlanTask.formatter.parseDateTime(startStr);
				DateTime end = SplitPlanTask.formatter.parseDateTime(endStr);

				if (end.getDayOfYear() - start.getDayOfYear() > 3) {
					isLong = true;
				}
			}
			
			for (Attributable act : plan.getActivities()) {
				if ("vacations".equalsIgnoreCase(act.getAttribute(Attributes.KEY.TYPE))) {
					if (isLong) {
						act.setAttribute(Attributes.KEY.TYPE, "vacations_long");
					} else {
						act.setAttribute(Attributes.KEY.TYPE, "vacations_short");
					}
				}
			}

			for (Segment leg : plan.getLegs()) {
				if ("vacations".equalsIgnoreCase(leg.getAttribute(Attributes.KEY.TRAVEL_PURPOSE))) {
					if (isLong) {
						leg.setAttribute(Attributes.KEY.TRAVEL_PURPOSE, "vacations_long");
					} else {
						leg.setAttribute(Attributes.KEY.TRAVEL_PURPOSE, "vacations_short");
					}
				}
			}

		}
	}

}
