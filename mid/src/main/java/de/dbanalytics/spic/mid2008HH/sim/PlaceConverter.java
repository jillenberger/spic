/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
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

package de.dbanalytics.spic.mid2008HH.sim;

import de.dbanalytics.spic.gis.Place;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.sim.data.Converter;

/**
 * Created by johannesillenberger on 07.06.17.
 */
public class PlaceConverter implements Converter {

    private final PlaceIndex index;

    public PlaceConverter(PlaceIndex index) {
        this.index = index;
    }

    @Override
    public Object toObject(String value) {
        return index.get(value);
    }

    @Override
    public String toString(Object value) {
        return ((Place) value).getId();
    }
}
