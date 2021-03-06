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
package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class EpisodePersonCollector<T> extends AbstractCollector<T, Person, Episode> {

    public EpisodePersonCollector(ValueProvider<T, Person> provider) {
        super(provider);
    }

    @Override
    public List<T> collect(Collection<? extends Person> persons) {
        List<T> values = new ArrayList<>(persons.size() * 2);
        for(Person p : persons) {
            for(Episode e : p.getEpisodes()) {
                if(predicate == null || predicate.test(e)) {
                    values.add(provider.get(p));
                }
            }
        }

        return values;
    }
}
