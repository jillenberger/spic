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

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Attributes;

/**
 * @author johannes
 *
 */
public class LegModeHandler implements LegAttributeHandler {

	@Override
	public void handle(Attributable leg, String key, String value) {
		setMode(key, "hvm1", value, "plane", leg);
		setMode(key, "hvm2", value, "rail", leg);
		setMode(key, "hvm3", value, "rail", leg);
		setMode(key, "hvm6", value, Attributes.MODE.CAR, leg);
		setMode(key, "hvm7", value, Attributes.MODE.CAR, leg);
		setMode(key, "hvm9", value, Attributes.MODE.CAR, leg);
		setMode(key, "hvm10", value, Attributes.MODE.CAR, leg);
	}

	private void setMode(String key, String modeKey, String value, String mode, Attributable leg) {
		if(key.endsWith(modeKey)) {
			if(value.equals("1")) {
				if(leg.setAttribute(Attributes.KEY.MODE, mode) != null) {
					System.err.println("Overwriting mode key");
				}
			}
		}
	}
}
