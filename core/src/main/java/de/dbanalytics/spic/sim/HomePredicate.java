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
import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.sim.data.CachedElement;
import de.dbanalytics.spic.sim.data.Converters;

/**
 * Created by johannesillenberger on 07.06.17.
 */
public class HomePredicate implements Predicate<CachedElement> {

    private final Object objectKey = Converters.newObjectKey();

    @Override
    public boolean test(CachedElement cachedElement) {
        Boolean result = (Boolean) cachedElement.getData(objectKey);

        if (result == null) {
            String type = cachedElement.getAttribute(CommonKeys.TYPE);
            result = ActivityTypes.HOME.equalsIgnoreCase(type);
            cachedElement.setData(objectKey, result);
        }

        return result;
    }
}
