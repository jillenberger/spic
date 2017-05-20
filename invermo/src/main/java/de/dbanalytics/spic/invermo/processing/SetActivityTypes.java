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
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.invermo.InvermoKeys;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 *
 */
public class SetActivityTypes implements EpisodeTask {

	@Override
	public void apply(Episode plan) {
		for(Attributable act : plan.getActivities()) {
			if(InvermoKeys.HOME.equals(act.getAttribute(InvermoKeys.LOCATION))) {
				act.setAttribute(CommonKeys.ACTIVITY_TYPE, InvermoKeys.HOME);
			}
		}
		
		for(int i = 0; i < plan.getLegs().size(); i++) {
			Attributable leg = plan.getLegs().get(i);
			Attributable act = plan.getActivities().get(i + 1);
			
			if(!InvermoKeys.HOME.equals(act.getAttribute(InvermoKeys.LOCATION))) {
				act.setAttribute(CommonKeys.ACTIVITY_TYPE, leg.getAttribute(CommonKeys.LEG_PURPOSE));
			}
		}

	}

}
