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

package de.dbanalytics.spic.source.invermo.generator;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.source.invermo.InvermoKeys;
import de.dbanalytics.spic.source.mid2008.generator.PersonAttributeHandler;

import java.util.Map;

/**
 * @author johannes
 * 
 */
public class WorkLocationHandler implements PersonAttributeHandler {

	@Override
	public void handle(Person person, Map<String, String> attributes) {
		String foreign = attributes.get(VariableNames.WORK_COUNTRY);
		if (VariableNames.validate(foreign)) {
			if (foreign.equals("1")) {
				String zip = attributes.get(VariableNames.WORK_ZIP);
				if(!VariableNames.validate(zip)) {
					zip = "";
				}
				
				String town = attributes.get(VariableNames.WORK_TOWN);
				if(!VariableNames.validate(town)) {
					town = "";
				}
				
				person.setAttribute(InvermoKeys.WORK_LOCATION, String.format("%s %s", zip, town));
				
			} else if (foreign.equals("2")) {
				String town = attributes.get("arbstadta");
				if(!VariableNames.validate(town)) {
					town = "";
				}
				
				String country = attributes.get("arblandname");
				if(!VariableNames.validate(country)) {
					country = "";
				}
				
				person.setAttribute(InvermoKeys.WORK_LOCATION, String.format("%s %s", town, country));
			}
		}
		
	}
}
