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
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Segment;

import java.util.Map;

/**
 * @author johannes
 *
 */
public class LegPurposeHandlerOld implements LegAttributeHandler {

	@Override
	public void handle(Segment leg, Map<String, String> attributes) {
		String val = attributes.get(VariableNames.LEG_MAIN_TYPE);

		if(val.equalsIgnoreCase("1")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.WORK);
			
		} else if(val.equalsIgnoreCase("2")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.BUSINESS);
			
		} else if(val.equalsIgnoreCase("3")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.EDUCATION);
			
		} else if(val.equalsIgnoreCase("4")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.SHOP);
			
		} else if(val.equalsIgnoreCase("5")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.PRIVATE);
			
		} else if(val.equalsIgnoreCase("6")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.PICKDROP);
			
		} else if(val.equalsIgnoreCase("7")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.LEISURE);
			
			String subtype = attributes.get(VariableNames.LEG_SUB_TYPE);
			if(subtype != null) {
				if(subtype.equalsIgnoreCase("701")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.VISIT);
				} else if(subtype.equalsIgnoreCase("702")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.CULTURE);
				} else if(subtype.equalsIgnoreCase("703")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.CULTURE);
				} else if(subtype.equalsIgnoreCase("701")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.SPORT);
				} else if(subtype.equalsIgnoreCase("706")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.GASTRO);
				} else if(subtype.equalsIgnoreCase("708")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.VACATIONS_SHORT);
				} else if(subtype.equalsIgnoreCase("709")) {
					leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.VACATIONS_LONG);
				}
			}
			
		} else if(val.equalsIgnoreCase("8")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.HOME);
			
		} else if(val.equalsIgnoreCase("31")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.EDUCATION);
			
		} else if(val.equalsIgnoreCase("32")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.EDUCATION);
			
		} else {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, ActivityTypes.MISC);
		}
		
	}

}
