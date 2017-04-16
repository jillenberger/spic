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

package de.dbanalytics.devel.matrix2014.source.mid2008;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 */
public class FilterLegDistance implements EpisodeTask {

    @Override
    public void apply(Episode episode) {
        for(Segment leg : episode.getLegs()) {
            String value = leg.getAttribute(CommonKeys.LEG_ROUTE_DISTANCE);
            if(value != null) {
                double d = Double.parseDouble(value);
                if(d > 400000) {
                    episode.setAttribute(CommonKeys.DELETE, CommonValues.TRUE);
                    break;
                }
            }
        }
    }
}
