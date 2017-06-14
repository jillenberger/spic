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

import de.dbanalytics.spic.analysis.Histogram;
import de.dbanalytics.spic.sim.RelativeErrorFunction;
import gnu.trove.map.TDoubleDoubleMap;
import org.matsim.core.config.ConfigGroup;

import java.io.IOException;

/**
 * Created by johannesillenberger on 14.06.17.
 */
public class LegDistributionTermConfig extends Configurator<LegDistributionTermBuilder> {

    private static final String ATTRIBUTE_KEY = "attribute_key";

    private static final String DISTRIBUTION_FILE = "distribution_file";

    private static final String DEBUG_INTERVAL = "debug_interval";

    private static final String ERROR_EXPONENT = "error_exponent";

    private static final String ERROR_INF_VALUE = "error_inf_value";

    private static final String LOG_INTERVAL = "log_interval";

    private static final String NAME = "name";

    private static final String RESET_INTERVAL = "reset_interval";

    private static final String START_ITERATION = "start_iteration";

    private static final String THETA_MAX = "theta_max";

    private static final String THETA_MIN = "theta_min";

    private static final String THETA_FACTOR = "theta_factor";

    private static final String THETA_INTERVAL = "theta_interval";

    private static final String THETA_THRESHOLD = "theta_threshold";

    public LegDistributionTermConfig(ConfigGroup config) {
        super(config);
    }

    @Override
    public LegDistributionTermBuilder configure() {
        try {
            String key = config.getValue(ATTRIBUTE_KEY);
            TDoubleDoubleMap hist = Histogram.readHistogram(config.getValue(DISTRIBUTION_FILE));
            LegDistributionTermBuilder builder = new LegDistributionTermBuilder(hist, key);
            return configure(builder);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public LegDistributionTermBuilder configure(LegDistributionTermBuilder builder) {
        /** debug interval */
        String value = config.getValue(DEBUG_INTERVAL);
        if (value != null) builder.debugInterval((long) Double.parseDouble(value));

        /** error function */
        double exponent = 1.0;
        value = config.getValue(ERROR_EXPONENT);
        if (value != null) exponent = Double.parseDouble(value);

        double infValue = 1.0;
        value = config.getValue(ERROR_INF_VALUE);
        if (value != null) infValue = Double.parseDouble(value);

        builder.errorFunction(new RelativeErrorFunction(exponent, infValue));

        /** logging interval */
        value = config.getValue(LOG_INTERVAL);
        if (value != null) builder.logInterval((long) Double.parseDouble(value));

        /** name */
        value = config.getValue(NAME);
        if (value != null) builder.name(value);

        /** reset interval */
        value = config.getValue(RESET_INTERVAL);
        if (value != null) builder.resetInterval((long) Double.parseDouble(value));

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
