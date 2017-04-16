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

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.sim.data.CachedSegment;

/**
 * @author jillenberger
 */
public class CachedModePredicate implements Predicate<CachedSegment> {

    private static Object dataKey = new Object();

    private final String key;

    private final String value;

    public CachedModePredicate(String key, String value) {
        this.key = key;
        this.value = value;
    }
    @Override
    public boolean test(CachedSegment cachedSegment) {
        Boolean isMode = (Boolean) cachedSegment.getData(dataKey);
        if(isMode == null) {
            isMode = value.equals(cachedSegment.getAttribute(key));
            cachedSegment.setData(dataKey, isMode);
        }
        return isMode;
    }
}
