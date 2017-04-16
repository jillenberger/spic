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

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import de.dbanalytics.spic.source.mid2008.MiDValues;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class PersonMonthHandler implements PersonAttributeHandler {

	@Override
	public void handle(Person person, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.SURVEY_MONTH);
		if(val != null) {
			if(val.equalsIgnoreCase("1")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.JANUARY);
			} else if(val.equalsIgnoreCase("2")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.FEBRUARY);
			} else if(val.equalsIgnoreCase("3")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.MARCH);
			} else if(val.equalsIgnoreCase("4")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.APRIL);
			} else if(val.equalsIgnoreCase("5")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.MAY);
			} else if(val.equalsIgnoreCase("6")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.JUNE);
			} else if(val.equalsIgnoreCase("7")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.JULY);
			} else if(val.equalsIgnoreCase("8")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.AUGUST);
			} else if(val.equalsIgnoreCase("9")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.SEPTEMBER);
			} else if(val.equalsIgnoreCase("10")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.OCTOBER);
			} else if(val.equalsIgnoreCase("11")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.NOVEMBER);
			} else if(val.equalsIgnoreCase("12")) {
				person.setAttribute(MiDKeys.PERSON_MONTH, MiDValues.DECEMBER);
			}
		}

	}

}
