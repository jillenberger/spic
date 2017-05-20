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

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.PlainSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author johannes
 * 
 */
public class LegHandlerAdaptor implements AttributeHandler<Episode> {

	private List<LegAttributeHandler> delegates = new ArrayList<LegAttributeHandler>();

	public void addHandler(LegAttributeHandler handler) {
		delegates.add(handler);
	}

	@Override
	public void handleAttribute(Episode plan, Map<String, String> attributes) {
		for (Entry<String, String> entry : attributes.entrySet()) {
			if (VariableNames.validate(entry.getValue())) {
				String key = entry.getKey();
				if (key.startsWith("e")) {
					int idx = Character.getNumericValue(key.charAt(1));
					idx = idx - 1;
					while (idx > plan.getLegs().size() - 1) {
						plan.addLeg(new PlainSegment());
					}

					for (LegAttributeHandler legHandler : delegates)
						legHandler.handle(plan.getLegs().get(idx), key, entry.getValue());
				}
			}
		}

	}

}
