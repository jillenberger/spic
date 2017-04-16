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

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DataPool {

	private static final Logger logger = Logger.getLogger(DataPool.class);
	
	private final Map<String, Object> dataObjects;
	
	private final Map<String, DataLoader> dataLoaders;
	
	public DataPool() {
		dataObjects = new HashMap<>();
		dataLoaders = new HashMap<>();
	}
	
	public void register(DataLoader loader, String key) {
		if(dataLoaders.containsKey(key)) {
			logger.warn(String.format("Cannot override the data loader for key \"%s\"", key));
		} else {
			dataLoaders.put(key, loader);
		}
	}
	
	public Object get(String key) {
		Object data = dataObjects.get(key);

		if(data == null) {
			loadData(key);
			data = dataObjects.get(key);
		}
		
		return data;
	}
	
	private synchronized void loadData(String key) {
		DataLoader loader = dataLoaders.get(key);
		if(loader == null) {
			logger.warn(String.format("No data loader for key \"%s\" found. Register the corresponding data loader first.", key));
		} else {
			Object data = loader.load();
			dataObjects.put(key, data);
		}
	}
}
