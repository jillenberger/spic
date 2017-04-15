/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
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

package de.dbanalytics.spic.source.mid2008.processing;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import de.dbanalytics.spic.source.mid2008.MiDValues;

/**
 * @author johannes
 */
public class ValidateDomestic implements EpisodeTask {

    @Override
    public void apply(Episode episode) {
        for(Segment leg : episode.getLegs()) {
            if(!MiDValues.DOMESTIC.equalsIgnoreCase(leg.getAttribute(MiDKeys.LEG_DESTINATION))) {
                episode.setAttribute(CommonKeys.DELETE, CommonValues.TRUE);
            }
        }
    }
}
