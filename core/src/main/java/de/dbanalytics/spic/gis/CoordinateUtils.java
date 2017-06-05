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

package de.dbanalytics.spic.gis;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author jillenberger
 */
public class CoordinateUtils {

    private static final String FIELD_SEPARATOR = " ";

    public static Coordinate parse(String string) {
        String tokens[] = string.split("\\s");

        double x = Double.parseDouble(tokens[0]);
        double y = Double.parseDouble(tokens[1]);
        double z = 0;
        if (tokens.length > 2) z = Double.parseDouble(tokens[2]);

        return new Coordinate(x, y, z);
    }

    public static String toString(Coordinate coordinate) {
        StringBuilder builder = new StringBuilder();

        builder.append(String.valueOf(coordinate.x));
        builder.append(FIELD_SEPARATOR);
        builder.append(String.valueOf(coordinate.y));

        if (coordinate.z != 0 && !Double.isNaN(coordinate.z)) {
            builder.append(FIELD_SEPARATOR);
            builder.append(String.valueOf(coordinate.z));
        }

        return builder.toString();
    }
}
