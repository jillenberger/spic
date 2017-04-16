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

package de.dbanalytics.spic.util;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;

import java.util.Set;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class PopulationStats {

    private int numPersons;
    private int numEpisodes;
    private int numActivities;
    private int numLegs;

    public int getNumPersons() {
        return numPersons;
    }

    public int getNumEpisodes() {
        return numEpisodes;
    }

    public int getNumActivities() {
        return numActivities;
    }

    public int getNumLegs() {
        return numLegs;
    }

    public void run(Set<? extends Person> persons) {
        numPersons = persons.size();
        for(Person person : persons) {
            for(Episode episode : person.getEpisodes()) {
                numActivities += episode.getActivities().size();
                numLegs += episode.getLegs().size();
            }
            numEpisodes += person.getEpisodes().size();
        }
    }
}
