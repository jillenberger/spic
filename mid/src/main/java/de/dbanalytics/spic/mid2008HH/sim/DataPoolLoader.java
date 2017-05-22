/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.dbanalytics.spic.mid2008HH.sim;

import de.dbanalytics.spic.gis.ActivityLocationLayerLoader;
import de.dbanalytics.spic.gis.DataPool;
import de.dbanalytics.spic.gis.FacilityDataLoader;
import de.dbanalytics.spic.gis.ZoneDataLoader;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;

/**
 * @author jillenberger
 */
public class DataPoolLoader {

    public static void load(Simulator engine, Config config) {
        DataPool dataPool = engine.getDataPool();
        ConfigGroup configGroup = config.getModule(Simulator.MODULE_NAME);

        dataPool.register(new FacilityDataLoader(
                        configGroup.getValue("facilities"),
                        configGroup.getValue("typeMapping"),
                        engine.getRandom()),
                FacilityDataLoader.KEY);
        dataPool.register(new ZoneDataLoader(configGroup), ZoneDataLoader.KEY);
        dataPool.register(new ActivityLocationLayerLoader(dataPool), ActivityLocationLayerLoader.KEY);
    }
}
