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
