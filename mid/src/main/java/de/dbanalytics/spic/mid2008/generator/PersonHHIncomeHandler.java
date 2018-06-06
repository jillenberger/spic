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

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.mid2008.MidAttributes;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class PersonHHIncomeHandler implements PersonAttributeHandler {

	@Override
	public void handle(Person person, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.HH_INCOME);
		
		if(val != null) {
			if(val.equalsIgnoreCase("1")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "250");
			else if(val.equalsIgnoreCase("2")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "700");
			else if(val.equalsIgnoreCase("3")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "1200");
			else if(val.equalsIgnoreCase("4")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "1750");
			else if(val.equalsIgnoreCase("5")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "2300");
			else if(val.equalsIgnoreCase("6")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "2800");
			else if(val.equalsIgnoreCase("7")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "3300");
			else if(val.equalsIgnoreCase("8")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "3800");
			else if(val.equalsIgnoreCase("9")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "4300");
			else if(val.equalsIgnoreCase("10")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "4800");
			else if(val.equalsIgnoreCase("11")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "5300");
			else if(val.equalsIgnoreCase("12")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "5800");
			else if(val.equalsIgnoreCase("13")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "6300");
			else if(val.equalsIgnoreCase("14")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "6800");
			else if(val.equalsIgnoreCase("15")) person.setAttribute(MidAttributes.KEY.HH_INCOME, "7300");
		}
	}
}
