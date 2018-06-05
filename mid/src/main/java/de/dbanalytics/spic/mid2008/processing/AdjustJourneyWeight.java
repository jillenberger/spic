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

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.data.Attributes;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.processing.PersonTask;

/**
 * @author johannes
 */
public class AdjustJourneyWeight implements PersonTask {

    @Override
    public void apply(Person person) {
        double weight = Double.parseDouble(person.getAttribute(Attributes.KEY.WEIGHT));
        weight = weight / 75.0;
//        weight = weight / 45.0; // 3 month time frame
//        weight = weight / 30.0; // 3 month time frame
//        weight = weight / 365.0;
        person.setAttribute(Attributes.KEY.WEIGHT, String.valueOf(weight));
    }
}
