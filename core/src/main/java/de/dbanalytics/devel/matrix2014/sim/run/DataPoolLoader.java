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
package de.dbanalytics.devel.matrix2014.sim.run;

import de.dbanalytics.devel.matrix2014.gis.ActivityLocationLayerLoader;
import de.dbanalytics.devel.matrix2014.gis.ValidateFacilities;
import de.dbanalytics.devel.matrix2014.gis.ZoneSetLAU2Class;
import de.dbanalytics.spic.gis.*;
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

        ValidateFacilities.validate(dataPool, "modena");
        ValidateFacilities.validate(dataPool, "lau2");
        ValidateFacilities.validate(dataPool, "nuts3");
//        ValidateFacilities.validate(dataPool, "tomtom");

        ZoneCollection lau2Zones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("lau2");
        new ZoneSetLAU2Class().apply(lau2Zones);
    }
}
