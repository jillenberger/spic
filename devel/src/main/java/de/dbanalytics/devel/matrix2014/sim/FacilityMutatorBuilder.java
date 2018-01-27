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
package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.devel.matrix2014.data.DataPool;
import de.dbanalytics.devel.matrix2014.gis.FacilityData;
import de.dbanalytics.devel.matrix2014.gis.FacilityDataLoader;
import de.dbanalytics.spic.sim.*;
import de.dbanalytics.devel.matrix2014.data.ActivityFacilityConverter;
import de.dbanalytics.spic.sim.data.Converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author jillenberger
 */
public class FacilityMutatorBuilder implements MutatorBuilder {

    private final Random random;

    private final FacilityData facilityData;

    private final List<String> blacklist;

    private AttributeChangeListener listener;

    private DataPool dataPool;

    private double proximityProba = 0.5;

    public FacilityMutatorBuilder(DataPool dataPool, Random random) {
        this.facilityData = (FacilityData) dataPool.get(FacilityDataLoader.KEY);
        this.random = random;
        this.dataPool = dataPool;
        blacklist = new ArrayList<>();
    }

    public void addToBlacklist(String type) {
        blacklist.add(type);
    }

    public void setListener(AttributeChangeListener listener) {
        this.listener = listener;
    }

    public void setProximityProbability(double proba) {
        this.proximityProba = proba;
    }

    @Override
    public Mutator build() {
        Object dataKey = Converters.register(CommonKeys.ACTIVITY_FACILITY, ActivityFacilityConverter.getInstance(facilityData));

//        RandomFacilityGenerator generator = new RandomFacilityGenerator(facilityData);
//        LocalFacilityGenerator generator = new LocalFacilityGenerator(facilityData, random);

//        ZoneData zoneData = (ZoneData) dataPool.get(ZoneDataLoader.KEY);
//        ZoneCollection zones = zoneData.getLayer("nuts3");
//        ProximityFacilityGenerator generator = new ProximityFacilityGenerator(facilityData, zones, proximityProba,
//                random);

        SegmentedFacilityGenerator generator = new SegmentedFacilityGenerator(dataPool, "modena", random);
        generator.setLocalSegmentProbability(proximityProba);
        for(String type : blacklist) {
            generator.addToBlacklist(type);
        }

        AttributeMutator attMutator = new AttributeMutator(dataKey, generator, listener);
        RandomActMutator actMutator = new RandomActMutator(attMutator, random);

        return actMutator;
    }

}
