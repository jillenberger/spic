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
package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.CommonValues;
import de.dbanalytics.spic.data.Person;

/**
 * @author jillenberger
 */
public class ValidatePersonWeight implements PersonTask {

    @Override
    public void apply(Person person) {
        double w = Double.parseDouble(person.getAttribute(Attributes.KEY.WEIGHT));
        boolean valid = true;
        if(Double.isInfinite(w)) valid = false;
        else if(Double.isNaN(w)) valid = false;
        else if(w == 0) valid = false;

        if(!valid) {
            person.setAttribute(Attributes.KEY.DELETE, CommonValues.TRUE);
        }
    }
}
