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
package de.dbanalytics.devel.matrix2014.config;

import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixTxtIO;
import de.dbanalytics.spic.matrix.ODCalibrator;
import org.matsim.core.config.ConfigGroup;

import java.io.IOException;

/**
 * @author jillenberger
 */
public class ODCalibratorConfigurator {

    private static final String FILE = "file";

    private static final String LAYER = "zoneLayer";

    private static final String DIST_THRESHOLD = "distanceThreshold";

    private static final String VOL_THRESHOLD = "volumeThreshold";

    private final DataPool dataPool;

    public ODCalibratorConfigurator(DataPool dataPool) {
        this.dataPool = dataPool;
    }

    public ODCalibrator configure(ConfigGroup config) {
        String file = config.getValue(FILE);
        String layer = config.getValue(LAYER);
        double distanceThreshold = Double.parseDouble(config.getValue(DIST_THRESHOLD));
        double volumeThreshold = Double.parseDouble(config.getValue(VOL_THRESHOLD));

        NumericMatrix refMatrix = new NumericMatrix();
        try {
            NumericMatrixTxtIO.read(refMatrix, file);
        } catch (IOException e) {
            e.printStackTrace();
        }


        FacilityData facilityData = (FacilityData) dataPool.get(FacilityDataLoader.KEY);
        ZoneData zoneData = (ZoneData) dataPool.get(ZoneDataLoader.KEY);
        ZoneCollection zones = zoneData.getLayer(layer);

        ODCalibrator calibrator = new ODCalibrator.Builder(refMatrix, zones, facilityData.getAll()).build();
        calibrator.setDistanceThreshold(distanceThreshold);
        calibrator.setVolumeThreshold(volumeThreshold);

        return calibrator;
    }
}
