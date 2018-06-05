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

package de.dbanalytics.spic.mid2008.generator;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Person;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class PersonDayHandler implements PersonAttributeHandler {

	@Override
	public void handle(Person person, Map<String, String> attributes) {
		String day = attributes.get(VariableNames.SURVEY_DAY);

		if(day.equalsIgnoreCase("1")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.MONDAY);
		} else if(day.equalsIgnoreCase("2")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.TUESDAY);
		} else if(day.equalsIgnoreCase("3")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.WEDNESDAY);
		} else if(day.equalsIgnoreCase("4")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.THURSDAY);
		} else if(day.equalsIgnoreCase("5")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.FRIDAY);
		} else if(day.equalsIgnoreCase("6")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.SATURDAY);
		} else if(day.equalsIgnoreCase("7")) {
			person.setAttribute(Attributes.KEY.WEEKDAY, Attributes.WEEKDAY.SUNDAY);
		}
	}

}
