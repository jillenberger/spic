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

package de.dbanalytics.spic.invermo.generator;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.CommonKeys;

/**
 * @author johannes
 *
 */
public class LegPurposeHandler implements LegAttributeHandler {

	@Override
	public void handle(Attributable leg, String key, String value) {
		if(key.endsWith("zweck1")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "vacations");
		} else if(key.endsWith("zweck2")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "shop");
		} else if(key.endsWith("zweck3")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "leisure");
		} else if(key.endsWith("zweck4")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "leisure");
		} else if(key.endsWith("zweck5")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "visit");
		} else if(key.endsWith("zweck6")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "culture");
		} else if(key.endsWith("zweck7")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "misc");
		} else if(key.endsWith("zweck8")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "business");
		} else if(key.endsWith("zweck9")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, "misc");
		} else if(key.endsWith("zweck10")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, getSubType(value));
		} else if(key.endsWith("zweck11")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, getSubType(value));
		} else if(key.endsWith("zweck12")) {
			leg.setAttribute(CommonKeys.LEG_PURPOSE, getSubType(value));
		}
	}
	
	private String getSubType(String code) {
		if(code.equalsIgnoreCase("51")) {
			return "gastro";
		} else if(code.equalsIgnoreCase("52")) {
			return ActivityTypes.LEISURE;
		} else if(code.equalsIgnoreCase("54")) {
			return "culture";
		} else if(code.equalsIgnoreCase("55")) {
			return "visit";
		} else if(code.equalsIgnoreCase("56")) {
			return "private";
		} else if(code.equalsIgnoreCase("57")) {
			return "private";
		} else if(code.equalsIgnoreCase("58")) {
			return "private";
		} else if(code.equalsIgnoreCase("59")) {
			return "private";
		} else if(code.equalsIgnoreCase("60")) {
			return "business";
		} else if(code.equalsIgnoreCase("61")) {
			return "business";
		} else if(code.equalsIgnoreCase("62")) {
			return "work";
		} else {
			return "misc";
		}
		
	}

}
