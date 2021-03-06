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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.analysis;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by johannesillenberger on 11.05.17.
 */
public class ActCollector<T> extends AbstractCollector<T, Segment, Segment> {

    public ActCollector(ValueProvider<T, Segment> provider) {
        super(provider);
    }

    @Override
    public List<T> collect(Collection<? extends Person> persons) {
        ArrayList<T> values = new ArrayList<>(persons.size() * 10);

        for (Person p : persons) {
            for (Episode e : p.getEpisodes()) {
                for (Segment leg : e.getActivities()) {
                    if (predicate == null || predicate.test(leg)) {
                        values.add(provider.get(leg));
                    }
                }
            }
        }

        values.trimToSize();

        return values;
    }
}
