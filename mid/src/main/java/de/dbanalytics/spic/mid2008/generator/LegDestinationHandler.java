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

package de.dbanalytics.spic.mid2008.generator;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.mid2008.MiDValues;

import java.util.Map;

/**
 * @author johannes
 */
public class LegDestinationHandler implements LegAttributeHandler {

    @Override
    public void handle(Segment leg, Map<String, String> attributes) {
        String val = attributes.get(VariableNames.LEG_DESTINATION);

        if(val.equalsIgnoreCase("1")) leg.setAttribute(MiDKeys.LEG_DESTINATION, ActivityTypes.HOME);
        else if(val.equalsIgnoreCase("2")) leg.setAttribute(MiDKeys.LEG_DESTINATION, ActivityTypes.WORK);
        else if(val.equalsIgnoreCase("3")) leg.setAttribute(MiDKeys.LEG_DESTINATION, MiDValues.IN_TOWN);
        else if(val.equalsIgnoreCase("4")) leg.setAttribute(MiDKeys.LEG_DESTINATION, MiDValues.OUT_OF_TOWN);
        else if(val.equalsIgnoreCase("5")) leg.setAttribute(MiDKeys.LEG_DESTINATION, MiDValues.ROUND_TRIP);
    }
}
