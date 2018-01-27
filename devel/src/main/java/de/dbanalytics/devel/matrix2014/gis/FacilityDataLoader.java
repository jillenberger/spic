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

package de.dbanalytics.devel.matrix2014.gis;

import de.dbanalytics.devel.matrix2014.data.DataLoader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.FacilitiesReaderMatsimV1;

import java.io.IOException;
import java.util.Random;

public class FacilityDataLoader implements DataLoader {

	private static final Logger logger = Logger.getLogger(FacilityDataLoader.class);
	
	public static final String KEY = "facilityData";
	
	private final String file;

	private final String mappingFile;
	
	private final Random random;
	
	public FacilityDataLoader(String file, String mappingFile, Random random) {
		this.file = file;
		this.random = random;
		this.mappingFile = mappingFile;
	}
	
	@Override
	public Object load() {
		logger.info("Loading facility data...");
		Level level = Logger.getRootLogger().getLevel();
		Logger.getRootLogger().setLevel(Level.WARN);
		
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		FacilitiesReaderMatsimV1 reader = new FacilitiesReaderMatsimV1(scenario);
		reader.readFile(file);

		FacilityData data = null;
		try {
			data = new FacilityData(scenario.getActivityFacilities(),
                    FacilityData.loadTypeMapping(mappingFile),
                    random);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Logger.getRootLogger().setLevel(level);
		
		logger.info(String.format("Loaded %s facilities.", data.getAll().getFacilities().size()));
		
		return data;
	}

}
