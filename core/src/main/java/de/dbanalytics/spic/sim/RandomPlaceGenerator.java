/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.sim.data.CachedElement;

import java.util.*;


/**
 * Created by johannesillenberger on 07.06.17.
 */
public class RandomPlaceGenerator implements ValueGenerator {

    private final Map<String, List<Place>> index;

    private final Random random;

    private Predicate<CachedElement> predicate;

    public RandomPlaceGenerator(Collection<Place> places, Random random) {
        this.random = random;

        index = new HashMap<>();

        for (Place place : places) {
            for (String type : place.getActivities()) {
                List<Place> list = index.get(type);
                if (list == null) {
                    list = new ArrayList<>();
                    index.put(type, list);
                }
                list.add(place);
            }
        }
    }

    public void setPredicate(Predicate<CachedElement> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Object newValue(CachedElement element) {
        if (predicate == null || predicate.test(element)) {
            String type = element.getAttribute(CommonKeys.TYPE);
            List<Place> places = index.get(type);
            if (places != null) return places.get(random.nextInt(places.size()));
            else return null;
        } else {
            return null;
        }
    }
}
