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
package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.PersonTask;

/**
 * @author jillenberger
 */
public class CopyPersonAttToLeg implements PersonTask {

    private final String key;

    public CopyPersonAttToLeg(String key) {
        this.key = key;
    }

    @Override
    public void apply(Person person) {
        String value = person.getAttribute(key);
        for(Episode e : person.getEpisodes()) {
            for(Segment leg : e.getLegs()) {
                leg.setAttribute(key, value);
            }
        }
    }
}