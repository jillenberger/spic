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

import java.util.Map;

/**
 * @author johannes
 *
 */
public class PersonMunicipalityClassHandler implements PersonAttributeHandler {

	@Override
	public void handle(Person person, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.PERSON_LAU2_CLASS);

		int cat = Integer.parseInt(val);
		cat = cat - 1; //TODO: Adapt simulation

		person.setAttribute(MiDKeys.PERSON_LAU2_CLASS, String.valueOf(cat));
	}

	private static int[][] categories;
	
	private static void initCategories() {
		categories = new int[6][2];
		categories[0][0] = 0;
		categories[0][1] = 5000;
		categories[1][0] = categories[0][1];
		categories[1][1] = 20000;
		categories[2][0] = categories[1][1];
		categories[2][1] = 50000;
		categories[3][0] = categories[2][1];
		categories[3][1] = 100000;
		categories[4][0] = categories[3][1];
		categories[4][1] = 500000;
		categories[5][0] = categories[4][1];
		categories[5][1] = Integer.MAX_VALUE;
	}
	
	public static int getLowerBound(int cat) {
		if(categories == null)
			initCategories();
		return categories[cat][0];
	}
	
	public static int getUpperBound(int cat) {
		if(categories == null)
			initCategories();
		return categories[cat][1];
	}
	
	public static int getCategory(int inhabs) {
		if(categories == null)
			initCategories();
		
		for(int i = 0; i < categories.length; i++) {
			if(categories[i][1] > inhabs) {
				return i;
			}
		}
		
		throw new RuntimeException("upps");
	}
}
