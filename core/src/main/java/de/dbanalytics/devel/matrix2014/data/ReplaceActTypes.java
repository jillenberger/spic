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

package de.dbanalytics.devel.matrix2014.data;

import de.dbanalytics.spic.data.ActivityTypes;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.processing.EpisodeTask;

import java.util.HashMap;
import java.util.Map;

/**
 * @author johannes
 * 
 */
public class ReplaceActTypes implements EpisodeTask {

	public static final String ORIGINAL_TYPE = "origType";

	private static Map<String, String> typeMapping;

	public Map<String, String> getTypeMapping() {
		if (typeMapping == null) {
			typeMapping = new HashMap<String, String>();
			typeMapping.put("vacations_short", ActivityTypes.LEISURE);
			typeMapping.put("vacations_long", ActivityTypes.LEISURE);
			typeMapping.put("visit", ActivityTypes.LEISURE);
			typeMapping.put("culture", ActivityTypes.LEISURE);
			typeMapping.put("gastro", ActivityTypes.LEISURE);
			typeMapping.put(ActivityTypes.BUSINESS, ActivityTypes.WORK);
			typeMapping.put("private", ActivityTypes.MISC);
			typeMapping.put("pickdrop", ActivityTypes.MISC);
			typeMapping.put("sport", ActivityTypes.LEISURE);
			typeMapping.put("wecommuter", ActivityTypes.WORK);
		}

		return typeMapping;
	}

	@Override
	public void apply(Episode plan) {
		for (Attributable act : plan.getActivities()) {
			String origType = act.getAttribute(ORIGINAL_TYPE);
			if (origType == null) {
				String type = act.getAttribute(CommonKeys.ACTIVITY_TYPE);
				act.setAttribute(ORIGINAL_TYPE, type);
				String newType = getTypeMapping().get(type);
				if (newType != null) {
					act.setAttribute(CommonKeys.ACTIVITY_TYPE, newType);
				}
			}
		}

	}

}
