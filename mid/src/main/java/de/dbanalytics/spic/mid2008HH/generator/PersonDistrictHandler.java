/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.mid2008HH.generator;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.mid2008.generator.PersonAttributeHandler;
import de.dbanalytics.spic.mid2008HH.MiDHHValues;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by johannesillenberger on 08.05.17.
 */
public class PersonDistrictHandler implements PersonAttributeHandler {

    private static final String DISTRICT_KEY = "sb_hvv";

    private static final Map<String, String> mapping;

    static {
        mapping = new HashMap<>();
        mapping.put("1", "Mitte");
        mapping.put("2", "Altona");
        mapping.put("3", "Eimsbüttel");
        mapping.put("4", "Nord");
        mapping.put("5", "Wandsbek");
        mapping.put("6", "Bergedorf");
        mapping.put("7", "Harburg");
    }


    @Override
    public void handle(Person person, Map<String, String> attributes) {
        String value = attributes.get(DISTRICT_KEY);
        if (value != null) {
            person.setAttribute(MiDHHValues.PERSON_DISTRICT, mapping.get(value));
        }
    }
}
