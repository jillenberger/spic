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

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.mid2008.MidAttributes;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 * 
 */
public class VacationsTypeTask implements EpisodeTask {

	@Override
	public void apply(Episode plan) {
		for (Attributable act : plan.getActivities()) {
			if (act.getAttribute(Attributes.KEY.TYPE).equalsIgnoreCase(ActivityTypes.LEISURE)) {
				String val = plan.getAttribute(MidAttributes.KEY.JOURNEY_DAYS);
				int days = 0;

				if (val != null)
					days = Integer.parseInt(val);
				
				if (days > 4) {
					act.setAttribute(Attributes.KEY.TYPE, ActivityTypes.VACATION_LONG);
				} else  if(days > 1 && days <= 4) {
					act.setAttribute(Attributes.KEY.TYPE, ActivityTypes.VACATION_SHORT);
				}

			}
		}

	}

}
