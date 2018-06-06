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

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.mid2008.MidAttributes;
import de.dbanalytics.spic.processing.EpisodeTask;
import org.joda.time.LocalDateTime;
import org.joda.time.Seconds;

import java.util.Locale;

/**
 * @author johannes
 *
 */
public class Date2TimeTask implements EpisodeTask {

	@Override
	public void apply(Episode plan) {
		LocalDateTime reference = null;
		
		for(Attributable leg : plan.getLegs()) {
			String start = leg.getAttribute(Attributes.KEY.DEPARTURE_TIME);
			if(start != null) {
				if(reference == null) {
					reference = getReference(start);
				}
				
				LocalDateTime startDate = SplitPlanTask.formatter.parseLocalDateTime(start);
				Seconds secs = Seconds.secondsBetween(reference, startDate);
				
				leg.setAttribute(Attributes.KEY.DEPARTURE_TIME, String.valueOf(secs.getSeconds()));
				if(!leg.keys().contains(MidAttributes.KEY.MONTH)) {
					setPlanDate(startDate, plan);
				}
			}
			
			String end = leg.getAttribute(Attributes.KEY.ARRIVAL_TIME);
			if(end != null) {
				if(reference == null) {
					reference = getReference(end);
				}
				
				LocalDateTime endDate = SplitPlanTask.formatter.parseLocalDateTime(end);
				Seconds secs = Seconds.secondsBetween(reference, endDate);
				
				leg.setAttribute(Attributes.KEY.ARRIVAL_TIME, String.valueOf(secs.getSeconds()));
				
				if(!leg.keys().contains(MidAttributes.KEY.MONTH)) {
					setPlanDate(endDate, plan);
				}
			}
		}

	}
	
	private LocalDateTime getReference(String date) {
		LocalDateTime dateTime = SplitPlanTask.formatter.parseLocalDateTime(date);
		return new LocalDateTime(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(), 0, 0);
	}
	
	private void setPlanDate(LocalDateTime dateTime, Episode plan) {
		plan.setAttribute(MidAttributes.KEY.MONTH, dateTime.monthOfYear().getAsShortText(Locale.US));
		plan.setAttribute(Attributes.KEY.WEEKDAY, dateTime.dayOfWeek().getAsShortText(Locale.US));
	}

}
