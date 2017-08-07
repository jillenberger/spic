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

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Factory;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PersonUtils;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author johannes
 */
public class IsolateEpisodes implements PersonTask {

    private final String attrKey;

    private final Factory factory;

    private final Map<String, Set<Person>> populations;

    public IsolateEpisodes(String attrKey, Factory factory) {
        this.attrKey = attrKey;
        this.factory = factory;
        populations = new HashMap<>();
    }

    @Override
    public void apply(Person person) {
        int idCnt = 0;

        for(Episode episode : person.getEpisodes()) {
            String key = episode.getAttribute(attrKey);
            Set<Person> persons = populations.get(key);
            if(persons == null) {
                persons = new LinkedHashSet<>();
                populations.put(key, persons);
            }

            String id = person.getId();
            if(person.getEpisodes().size() > 1) {
                id = String.format("%s.%s", person.getId(), idCnt);
                idCnt++;
            }
            Person clone = PersonUtils.shallowCopy(person, id, factory);
            clone.addEpisode(PersonUtils.deepCopy(episode, factory));

            persons.add(clone);
        }
    }

    public Map<String, Set<Person>> getPopulations() {
        return populations;
    }
}
