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
import de.dbanalytics.spic.processing.PersonTask;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 * 
 */
public class ReturnEpisodeTask implements PersonTask {

	private static final Logger logger = Logger.getLogger(ReturnEpisodeTask.class);

	@Override
	public void apply(Person person) {
		int count = 0;

		Set<Episode> journeys = new HashSet<>();
		for (Episode p : person.getEpisodes()) {
			if(p.getLegs().size() == 1)
				journeys.add(p);
			 else {
				count++;
			}
		}

		if(count > 0) {
			logger.warn(String.format("There are %s episodes with more than one leg. Are you sure this is a journeys " +
					"file?", count));
		}

		PlainFactory factory = new PlainFactory();
		for(Episode episode : journeys) {
			Episode returnEpisode = PersonUtils.shallowCopy(episode, factory);

			for(int i = episode.getActivities().size() - 1; i >= 0; i--) {
				Segment clone = PersonUtils.shallowCopy(episode.getActivities().get(i), factory);
				returnEpisode.addActivity(clone);
			}

			for(int i = episode.getLegs().size() - 1; i >= 0; i--) {
				Segment clone = PersonUtils.shallowCopy(episode.getLegs().get(i), factory);
				clone.removeAttribute(MidAttributes.KEY.ORIGIN);
				returnEpisode.addLeg(clone);
			}

			person.addEpisode(returnEpisode);
		}
	}
}
