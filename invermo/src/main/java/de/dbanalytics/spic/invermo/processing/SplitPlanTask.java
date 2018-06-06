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
import de.dbanalytics.spic.mid2008.MidAttributes;
import de.dbanalytics.spic.processing.PersonTask;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author johannes
 * 
 */
public class SplitPlanTask implements PersonTask {
	
	private static final Logger logger = Logger.getLogger(SplitPlanTask.class);

	public static DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

	@Override
	public void apply(Person person) {
		List<Episode> newPlans = new ArrayList<Episode>();
		
		for (Episode plan : person.getEpisodes()) {
			splitPlan(plan, newPlans);
		}

		person.getEpisodes().clear();
		
		for(Episode plan : newPlans)
			person.addEpisode(plan);
	}
	
	private void splitPlan(Episode plan, List<Episode> newPlans) {
		Episode subPlan = new PlainEpisode();

		DateTime prev = getDate(plan.getLegs().get(0));
		if(prev == null) {
			logger.warn("Cannot split plan. Neither start nor end time specified.");
			return;
		}
		
		for (int i = 0; i < plan.getLegs().size(); i++) {
			Attributable leg = plan.getLegs().get(i);
			Attributable act = plan.getActivities().get(i);
			
			DateTime current = getDate(leg);
			if(current == null) {
				logger.warn("Cannot split plan. Neither start nor end time specified.");
				return;
			}
			
			int currentDays = current.dayOfYear().get() + (365 * current.year().get());
			int prevDays = prev.dayOfYear().get() + (365 * prev.year().get());
			int nights = currentDays - prevDays;
			
//			if (current.dayOfYear().get() != prev.dayOfYear().get()) {
			if (nights > 0) {
				subPlan.setAttribute(MidAttributes.KEY.JOURNEY_DAYS, String.valueOf(nights + 1));

				subPlan.addActivity(((PlainSegment)act).clone());
				newPlans.add(subPlan);
				
				subPlan = new PlainEpisode();
				subPlan.addActivity(((PlainSegment)act).clone());
				subPlan.addLeg(((PlainSegment)leg).clone());
			} else {
				subPlan.setAttribute(MidAttributes.KEY.JOURNEY_DAYS, String.valueOf(nights + 1));
				
				subPlan.addActivity(((PlainSegment)act).clone());
				subPlan.addLeg(((PlainSegment)leg).clone());
			}
			
			prev = current;
		}
		
		int size = plan.getActivities().size();
		subPlan.addActivity(((PlainSegment)plan.getActivities().get(size - 1)).clone());
		newPlans.add(subPlan);
	}
	
	private DateTime getDate(Attributable leg) {
		String time = leg.getAttribute(Attributes.KEY.DEPARTURE_TIME);
		if(time == null) {
			/*
			 * This may have undesired effects in the case of over night trips.
			 */
			time = leg.getAttribute(Attributes.KEY.ARRIVAL_TIME);
		}
		
		if(time != null) {
			return formatter.parseDateTime(time);
		} else {
			return null;
		}
	}

}
