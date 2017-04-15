/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.spic.source.invermo.processing;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.source.invermo.InvermoKeys;

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
