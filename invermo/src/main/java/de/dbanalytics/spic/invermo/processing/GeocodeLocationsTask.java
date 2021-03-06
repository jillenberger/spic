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

package de.dbanalytics.spic.invermo.processing;

import com.google.code.geocoder.model.LatLng;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.invermo.InvermoKeys;
import de.dbanalytics.spic.processing.PersonTask;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author johannes
 * 
 */
public class GeocodeLocationsTask implements PersonTask {

	private final GoogleGeoCoder geoCoder;

	private Map<String, String> cache = new HashMap<String, String>();

	private String cacheFile;

	public GeocodeLocationsTask() {
		geoCoder = new GoogleGeoCoder();
	}

	public GeocodeLocationsTask(String host, int port) {
		geoCoder = new GoogleGeoCoder(host, port, 50);
	}

	public void setCacheFile(String file) {
		this.cacheFile = file;
		try {
			File theFile = new File(file);
			if (theFile.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line;
				while ((line = reader.readLine()) != null) {
					String tokens[] = line.split("\t", -1);
					cache.put(tokens[0], tokens[1]);
				}

				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void apply(Person person) {
		geocodeField(InvermoKeys.HOME_LOCATION, InvermoKeys.HOME_COORD, person);
		for (Episode plan : person.getEpisodes()) {
			for (Attributable act : plan.getActivities()) {
				geocodeField("location", "coord", act);
			}
		}

	}

	private void geocodeField(String key1, String key2, Attributable obj) {
		String start = obj.getAttribute(key1);
		if (start != null && !start.equalsIgnoreCase("home") && !start.equalsIgnoreCase("work") && !start.equalsIgnoreCase("prev")) {
			String str = cache.get(start);
//			if (str == null) {
//				LatLng coord = geoCoder.requestCoordinate(start);
//				if (coord != null) {
//					str = makeCoodinateString(coord);
//					cache.put(start, str);
//				}
//			}
			obj.setAttribute(key2, str);
		}
	}

	private String makeCoodinateString(LatLng coord) {
		StringBuilder builder = new StringBuilder(100);
		builder.append(String.valueOf(coord.getLng()));
		builder.append(",");
		builder.append(String.valueOf(coord.getLat()));

		return builder.toString();
	}

	public void writeCache() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile));
			for (Entry<String, String> entry : cache.entrySet()) {
				writer.write(entry.getKey());
				writer.write("\t");
				writer.write(entry.getValue());
				writer.newLine();
			}

			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
