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

package de.dbanalytics.spic.invermo.generator;

import de.dbanalytics.spic.data.PlainElement;
import de.dbanalytics.spic.invermo.InvermoKeys;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class HouseholdWeigthHandler implements AttributeHandler<PlainElement> {

	@Override
	public void handleAttribute(PlainElement object, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.MOB_WEIGTH);
		if(VariableNames.validate(val)) {
			object.setAttribute(InvermoKeys.WEIGHT, val);
		}
	}

}
