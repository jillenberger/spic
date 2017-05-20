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
import de.dbanalytics.spic.mid2008.MiDKeys;

import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 *
 */
public class PersonNUTS1Handler implements PersonAttributeHandler {

	private static Map<String, String> labels = new HashMap<>();

	static {
		labels.put("1", "Schleswig-Holstein");
		labels.put("2", "Hamburg");
		labels.put("3", "Niedersachsen");
		labels.put("4", "Bremen");
		labels.put("5", "Nordrhein-Westfalen");
		labels.put("6", "Hessen");
		labels.put("7", "Rheinland-Pfalz");
		labels.put("8", "Baden-Württemberg");
		labels.put("9", "Bayern");
		labels.put("10", "Saarland");
		labels.put("11", "Berlin");
		labels.put("12", "Brandenburg");
		labels.put("13", "Mecklenburg-Vorpommern");
		labels.put("14", "Sachsen");
		labels.put("15", "Sachsen-Anhalt");
		labels.put("16", "Thüringen");
	}

	@Override
	public void handle(Person person, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.PERSON_STATE);
		if(val != null) {
			person.setAttribute(MiDKeys.PERSON_NUTS1, labels.get(val));
		}

	}

}
