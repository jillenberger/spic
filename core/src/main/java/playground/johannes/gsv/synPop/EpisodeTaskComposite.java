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

package playground.johannes.gsv.synPop;

import org.matsim.contrib.common.collections.Composite;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.processing.EpisodeTask;

/**
 * @author johannes
 *
 */
public class EpisodeTaskComposite extends Composite<EpisodeTask> implements EpisodeTask {

	/* (non-Javadoc)
	 * @see de.dbanalytics.spic.processing.EpisodeTask#apply(de.dbanalytics.spic.data.PlainEpisode)
	 */
	@Override
	public void apply(Episode plan) {
		for(EpisodeTask task : components) {
			task.apply(plan);
		}
	}

}
