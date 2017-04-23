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

package de.dbanalytics.devel.matrix2014.analysis;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import de.dbanalytics.spic.source.mid2008.MiDValues;

/**
 * @author johannes
 */
public class SetSeason implements PersonTask {

    public static final String SEASON_KEY = "season";

    public static final String WINTER = "winter";

    public static final String SUMMER = "summer";

    @Override
    public void apply(Person person) {
        String month = person.getAttribute(MiDKeys.PERSON_MONTH);
        if(month != null) {
            String season = SUMMER;
            if (MiDValues.NOVEMBER.equalsIgnoreCase(month) ||
                    MiDValues.DECEMBER.equalsIgnoreCase(month) ||
                    MiDValues.JANUARY.equalsIgnoreCase(month) ||
                    MiDValues.FEBRUARY.equalsIgnoreCase(month) ||
                    MiDValues.MARCH.equalsIgnoreCase(month)) {
                season = WINTER;
            }

            person.setAttribute(SEASON_KEY, season);
        }
    }
}