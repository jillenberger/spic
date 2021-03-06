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
 */
public class PersonCarAvailHandler implements PersonAttributeHandler {

    @Override
    public void handle(Person person, Map<String, String> attributes) {
        String val = attributes.get(VariableNames.PERSON_CARAVAIL);

        if(val != null) {
            if(val.equalsIgnoreCase("1")) person.setAttribute(MidAttributes.KEY.CAR_AVAILABILITY, MidAttributes.CAR_AVAILABILITY.ALWAYS);
            if(val.equalsIgnoreCase("2")) person.setAttribute(MidAttributes.KEY.CAR_AVAILABILITY, MidAttributes.CAR_AVAILABILITY.SOMETIMES);
            if(val.equalsIgnoreCase("3")) person.setAttribute(MidAttributes.KEY.CAR_AVAILABILITY, MidAttributes.CAR_AVAILABILITY.NEVER);
            if(val.equalsIgnoreCase("4")) person.setAttribute(MidAttributes.KEY.CAR_AVAILABILITY, MidAttributes.CAR_AVAILABILITY.NEVER);
        }
    }
}
