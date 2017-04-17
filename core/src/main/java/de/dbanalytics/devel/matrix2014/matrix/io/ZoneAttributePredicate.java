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

package de.dbanalytics.devel.matrix2014.matrix.io;

import de.dbanalytics.spic.gis.Zone;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.matrix.Matrix;
import de.dbanalytics.spic.matrix.ODPredicate;
import org.apache.log4j.Logger;

/**
 * @author johannes
 */
public class ZoneAttributePredicate implements ODPredicate<String, Double> {

    private static final Logger logger = Logger.getLogger(ZoneAttributePredicate.class);

    private final ZoneCollection zones;

    private final String key;

    private final String value;

    public ZoneAttributePredicate(String key, String value, ZoneCollection zones) {
        this.key = key;
        this.value = value;
        this.zones = zones;
    }

    @Override
    public boolean test(String row, String col, Matrix<String, Double> matrix) {
        Zone zone_i = zones.get(row);
        Zone zone_j = zones.get(col);

        if (zone_i != null && zone_j != null) {
            return (value.equals(zone_i.getAttribute(key)) && value.equals(zone_j.getAttribute(key)));
        } else {
            if (zone_i == null)
                logger.warn(String.format("Zone not found: %s", row));

            if (zone_j == null)
                logger.warn(String.format("Zone not found: %s", col));

            return false;
        }
    }
}
