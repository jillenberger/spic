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

package de.dbanalytics.spic.mid2008HH.generator;

import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.mid2008.generator.LegAttributeHandler;

import java.util.Map;

/**
 * Created by johannesillenberger on 23.05.17.
 */
public class LegNumPersonsHandler implements LegAttributeHandler {

    public static final String KEY = "anzpers";

    @Override
    public void handle(Segment leg, Map<String, String> attributes) {
        String value = attributes.get(KEY);
        if (value != null) {
            int num = (int) Double.parseDouble(value);
            if (num >= 1 && num <= 11) {
                leg.setAttribute(MiDKeys.LEG_PERSONS, String.valueOf(num));
            }
        }
    }
}
