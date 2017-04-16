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

package de.dbanalytics.spic.source.invermo.processing;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.source.invermo.InvermoKeys;

/**
 * @author johannes
 * 
 */
public class SetActivityLocations implements EpisodeTask {

	@Override
	public void apply(Episode plan) {

		for (int i = 0; i < plan.getLegs().size(); i++) {
			Attributable leg = plan.getLegs().get(i);

			Attributable prev = plan.getActivities().get(i);
			Attributable next = plan.getActivities().get(i + 1);

			String startLoc = leg.getAttribute(InvermoKeys.START_LOCATION);
			if (startLoc != null) {
				if (startLoc.equals(InvermoKeys.HOME)) {
					prev.setAttribute(InvermoKeys.LOCATION, InvermoKeys.HOME);
				} else if (startLoc.equals(InvermoKeys.WORK)) {
					prev.setAttribute(InvermoKeys.LOCATION, InvermoKeys.WORK);
				} else if (startLoc.equals(InvermoKeys.PREV)) {

				}
			}

			String destLoc = leg.getAttribute(InvermoKeys.DESTINATION_LOCATION);
			if (destLoc != null) {
				if (destLoc.equals(InvermoKeys.HOME)) {
					next.setAttribute(InvermoKeys.LOCATION, InvermoKeys.HOME);
				} else if (destLoc.equals(InvermoKeys.WORK)) {
					next.setAttribute(InvermoKeys.LOCATION, InvermoKeys.WORK);
				} else {
					next.setAttribute(InvermoKeys.LOCATION, destLoc);
				}
			}
		}

	}

}
