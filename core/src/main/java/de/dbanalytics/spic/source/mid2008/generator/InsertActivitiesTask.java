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

package de.dbanalytics.spic.source.mid2008.generator;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Factory;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 */
public class InsertActivitiesTask implements EpisodeTask {

    private final Factory factory;

    public InsertActivitiesTask(Factory factory) {
        this.factory = factory;
    }

    @Override
    public void apply(Episode episode) {
        int nLegs = episode.getLegs().size();

        for (int i = 0; i < nLegs + 1; i++) {
            Segment activity = factory.newSegment();
            episode.addActivity(activity);
        }

    }

}
