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

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.invermo.InvermoKeys;
import de.dbanalytics.spic.processing.PersonTask;

/**
 * @author johannes
 *
 */
public class InsertHomePlanTask implements PersonTask {

	@Override
	public void apply(Person person) {
		if(person.getEpisodes().isEmpty()) {
			Episode plan = new PlainEpisode();
			PlainSegment act = new PlainSegment();
			act.setAttribute(Attributes.KEY.TYPE, "home");
			act.setAttribute(Attributes.KEY.START_TIME, "0");
			act.setAttribute(Attributes.KEY.END_TIME, "86400");
			act.setAttribute(InvermoKeys.LOCATION, "home");
			
			plan.addActivity(act);
			person.addEpisode(plan);
		}

	}

}
