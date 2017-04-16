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

import de.dbanalytics.spic.gis.DataLoader;
import de.dbanalytics.spic.gis.DataPool;
import de.dbanalytics.spic.gis.FacilityData;
import de.dbanalytics.spic.gis.FacilityDataLoader;

/**
 * @author johannes
 */
public class ActivityLocationLayerLoader implements DataLoader {

    public static final String KEY = "activityLocationLayer";

    private final DataPool dataPool;

    public ActivityLocationLayerLoader(DataPool dataPool) {
        this.dataPool = dataPool;
    }

    @Override
    public Object load() {
        FacilityData fData = (FacilityData) dataPool.get(FacilityDataLoader.KEY);
        return new ActivityLocationLayer(fData.getAll());
    }
}
