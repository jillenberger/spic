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

package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.data.Episode;

/**
 * @author johannes
 *
 */
public class ValidateMissingLegTimes implements EpisodeTask {

	@Override
	public void apply(Episode episode) {
		for(Attributable leg : episode.getLegs()) {
			String start = leg.getAttribute(CommonKeys.LEG_START_TIME);
			String end = leg.getAttribute(CommonKeys.LEG_END_TIME);

			if(start == null || end == null) {
				episode.setAttribute(CommonKeys.DELETE, CommonValues.TRUE);
				return;
			}
		}

	}
}