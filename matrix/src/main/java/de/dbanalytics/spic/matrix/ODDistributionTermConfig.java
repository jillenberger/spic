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

import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.util.Configurator;
import org.matsim.core.config.ConfigGroup;
import org.matsim.facilities.ActivityFacilities;

/**
 * @author johannes
 */
public class ODDistributionTermConfig extends Configurator<ODDistributionTermBuilder> {

    private static final String DISTANCE_THRESHOLD = "distance_threshold";

    private static final String VOLUME_THRESHOLD = "volume_threshold";

    private static final String LOG_INTERVAL = "log_interval";

    private static final String NAME = "name";

    private static final String START_ITERATION = "start_iteration";

    private static final String THETA_MAX = "theta_max";

    private static final String THETA_MIN = "theta_min";

    private static final String THETA_FACTOR = "theta_factor";

    private static final String THETA_INTERVAL = "theta_interval";

    private static final String THETA_THRESHOLD = "theta_threshold";

    private static final String MATRIX_FILE = "matrix_file";

    private ActivityFacilities facilities;

    private ZoneCollection zones;

    public ODDistributionTermConfig(ConfigGroup config, ActivityFacilities facilities, ZoneCollection zones) {
        super(config);
        this.facilities = facilities;
        this.zones = zones;
    }

    @Override
    public ODDistributionTermBuilder configure() {
        String file = config.getValue(MATRIX_FILE);
        if (file == null) {
            throw new RuntimeException("Matrix file must be specified.");
        } else {
            NumericMatrix m = NumericMatrixIO.read(file);
            return new ODDistributionTermBuilder(m, facilities, zones);
        }


    }

    @Override
    public ODDistributionTermBuilder configure(ODDistributionTermBuilder builder) {
        /** distance threshold */
        String value = config.getValue(DISTANCE_THRESHOLD);
        if (value != null) builder.distanceThreshold(Double.parseDouble(value));

        /** volume threshold */
        value = config.getValue(VOLUME_THRESHOLD);
        if (value != null) builder.volumeThreshold(Double.parseDouble(value));

        /** logging interval */
        value = config.getValue(LOG_INTERVAL);
        if (value != null) builder.logInterval((long) Double.parseDouble(value));

        /** name */
        value = config.getValue(NAME);
        if (value != null) builder.name(value);

        /** start iteration */
        value = config.getValue(START_ITERATION);
        if (value != null) builder.startIteration((long) Double.parseDouble(value));

        /** thetas */
        value = config.getValue(THETA_MAX);
        if (value != null) builder.thetaMax(Double.parseDouble(value));

        value = config.getValue(THETA_MIN);
        if (value != null) builder.thetaMin(Double.parseDouble(value));

        value = config.getValue(THETA_FACTOR);
        if (value != null) builder.thetaFactor(Double.parseDouble(value));

        value = config.getValue(THETA_INTERVAL);
        if (value != null) builder.thetaInterval((long) Double.parseDouble(value));

        value = config.getValue(THETA_THRESHOLD);
        if (value != null) builder.thetaThreshold(Double.parseDouble(value));


        return builder;
    }
}
