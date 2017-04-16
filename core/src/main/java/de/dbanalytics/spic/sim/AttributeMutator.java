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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.sim.data.CachedElement;

/**
 * @author johannes
 */
public class AttributeMutator implements RandomElementMutator {

    private final Object dataKey;

    private final AttributeChangeListener listener;

    private final ValueGenerator generator;

    private Object oldValue;

    public AttributeMutator(Object dataKey, ValueGenerator generator, AttributeChangeListener listener) {
        this.dataKey = dataKey;
        this.listener = listener;
        this.generator = generator;
    }

    @Override
    public boolean modify(CachedElement element) {
        oldValue = element.getData(dataKey);
        Object newValue = generator.newValue(element);

        if(newValue != null) {
            element.setData(dataKey, newValue);

            if (listener != null) listener.onChange(dataKey, oldValue, newValue, element);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void revert(CachedElement element) {
        Object newValue = element.getData(dataKey);
        element.setData(dataKey, oldValue);

        if (listener != null) listener.onChange(dataKey, newValue, oldValue, element);

    }
}
