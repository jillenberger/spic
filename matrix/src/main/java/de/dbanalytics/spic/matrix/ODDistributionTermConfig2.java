/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.job.Configurator;
import org.apache.commons.configuration2.HierarchicalConfiguration;

/**
 * @author johannes
 */
public class ODDistributionTermConfig2 implements Configurator<ODDistributionTermBuilder2> {

    /** @deprecated */
    private static final String DISTANCE_THRESHOLD = "distanceThreshold";

    private static final String MIN_DISTANCE_THRESHOLD = "minDistanceThreshold";

    private static final String MAX_DISTANCE_THRESHOLD = "maxDistanceThreshold";

    private static final String VOLUME_THRESHOLD = "volumeThreshold";

    private static final String LOG_INTERVAL = "logInterval";

    private static final String DEBUG_INTERVAL = "debugInterval";

    private static final String NAME = "name";

    private static final String START_ITERATION = "startIteration";

    private static final String THETA_MAX = "thetaMax";

    private static final String THETA_MIN = "thetaMin";

    private static final String THETA_FACTOR = "thetaFactor";

    private static final String THETA_INTERVAL = "thetaInterval";

    private static final String THETA_THRESHOLD = "thetaThreshold";

    private static final String RESET_INTERVAL = "resetInterval";

    private static final String MATRIX_FILE = "matrixFile";

    private static final String ZONES_FILE = "zonesFile";

    private static final String PLACES_FILE = "placesFile";

    private static final String EPSG = "epsg";

    @Override
    public ODDistributionTermBuilder2 configure(HierarchicalConfiguration config) {
        ZoneIndex zoneIndex = (ZoneIndex) DataPool.get(
                config.getString(ZONES_FILE),
                new ZoneIndexLoader(config.getInt(EPSG)));
        PlaceIndex placeIndex = (PlaceIndex) DataPool.get(
                config.getString(PLACES_FILE),
                new PlaceIndexLoader(config.getInt(EPSG)));
        NumericMatrix m = NumericMatrixIO.read(config.getString(MATRIX_FILE));

        ODDistributionTermBuilder2 builder = new ODDistributionTermBuilder2(m, placeIndex, zoneIndex);
        return configure(config, builder);
    }

    @Override
    public ODDistributionTermBuilder2 configure(HierarchicalConfiguration config, ODDistributionTermBuilder2 builder) {
        /** distance threshold */
        builder.minDistanceThreshold(config.getDouble(DISTANCE_THRESHOLD, 0));
//        String value = config.getValue(DISTANCE_THRESHOLD);
//        if (value != null) builder.minDistanceThreshold(Double.parseDouble(value));
        //TODO: min_distance_threhold overwrites distance_threshold for compatibility
        if (config.containsKey(MIN_DISTANCE_THRESHOLD)) {
            builder.minDistanceThreshold(config.getDouble(MIN_DISTANCE_THRESHOLD));
        }
//        value = config.getValue(MIN_DISTANCE_THRESHOLD);
//        if (value != null) builder.minDistanceThreshold(Double.parseDouble(value));
        builder.maxDistanceThreshold(config.getDouble(MAX_DISTANCE_THRESHOLD, Double.MAX_VALUE));
//        value = config.getValue(MAX_DISTANCE_THRESHOLD);
//        if (value != null) builder.maxDistanceThreshold(Double.parseDouble(value));

        /** volume threshold */
        builder.volumeThreshold(config.getDouble(VOLUME_THRESHOLD, 0));
//        value = config.getValue(VOLUME_THRESHOLD);
//        if (value != null) builder.volumeThreshold(Double.parseDouble(value));

        /** logging interval */
        builder.logInterval((long) config.getDouble(LOG_INTERVAL, 0));
//        value = config.getValue(LOG_INTERVAL);
//        if (value != null) builder.logInterval((long) Double.parseDouble(value));

        /** debug interval */
        builder.debugInterval((long) config.getDouble(DEBUG_INTERVAL, 0));
//        value = config.getValue(DEBUG_INTERVAL);
//        if (value != null) builder.debugInterval((long) Double.parseDouble(value));

        /** reset interval */
        builder.resetInterval((long) config.getDouble(RESET_INTERVAL, Double.MAX_VALUE));
//        value = config.getValue(RESET_INTERVAL);
//        if (value != null) builder.resetInterval((long) Double.parseDouble(value));

        /** name */
        if (config.containsKey(NAME)) builder.name(config.getString(NAME));
//        value = config.getValue(NAME);
//        if (value != null) builder.name(value);

        /** start iteration */
        builder.startIteration((long) config.getDouble(START_ITERATION, 0));
//        value = config.getValue(START_ITERATION);
//        if (value != null) builder.startIteration((long) Double.parseDouble(value));

        /** thetas */
        builder.thetaMax(config.getDouble(THETA_MAX, 1));
//        value = config.getValue(THETA_MAX);
//        if (value != null) builder.thetaMax(Double.parseDouble(value));

        builder.thetaMin(config.getDouble(THETA_MIN, 1));
//        value = config.getValue(THETA_MIN);
//        if (value != null) builder.thetaMin(Double.parseDouble(value));

        builder.thetaFactor(config.getDouble(THETA_FACTOR, 10));
//        value = config.getValue(THETA_FACTOR);
//        if (value != null) builder.thetaFactor(Double.parseDouble(value));

        builder.thetaInterval((long) config.getDouble(THETA_INTERVAL, 1E7));
//        value = config.getValue(THETA_INTERVAL);
//        if (value != null) builder.thetaInterval((long) Double.parseDouble(value));

        builder.thetaThreshold(config.getDouble(THETA_THRESHOLD, 0.005));
//        value = config.getValue(THETA_THRESHOLD);
//        if (value != null) builder.thetaThreshold(Double.parseDouble(value));


        return builder;
    }
}
