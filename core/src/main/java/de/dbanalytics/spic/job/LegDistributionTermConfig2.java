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

package de.dbanalytics.spic.job;

import de.dbanalytics.spic.analysis.Histogram;
import de.dbanalytics.spic.sim.LegDistributionTermBuilder2;
import de.dbanalytics.spic.sim.RelativeErrorFunction;
import gnu.trove.map.TDoubleDoubleMap;
import org.apache.commons.configuration2.HierarchicalConfiguration;

import java.io.IOException;

/**
 * Created by johannesillenberger on 14.06.17.
 */
public class LegDistributionTermConfig2 implements Configurator<LegDistributionTermBuilder2> {

    private static final String ATTRIBUTE_KEY = "attributeKey";

    private static final String HISTOGRAM_FILE = "histogramFile";

    private static final String DEBUG_INTERVAL = "debugInterval";

    private static final String ERROR_EXPONENT = "errorExponent";

    private static final String ERROR_INF_VALUE = "errorInfValue";

    private static final String LOG_INTERVAL = "logInterval";

    private static final String NAME = "name";

    private static final String RESET_INTERVAL = "resetInterval";

    private static final String START_ITERATION = "startIteration";

    private static final String THETA_MAX = "thetaMax";

    private static final String THETA_MIN = "thetaMin";

    private static final String THETA_FACTOR = "thetaFactor";

    private static final String THETA_INTERVAL = "thetaInterval";

    private static final String THETA_THRESHOLD = "thetaThreshold";

    @Override
    public LegDistributionTermBuilder2 configure(HierarchicalConfiguration config) {
        try {
            String key = config.getString(ATTRIBUTE_KEY);
            TDoubleDoubleMap hist = Histogram.readHistogram(config.getString(HISTOGRAM_FILE));
            LegDistributionTermBuilder2 builder = new LegDistributionTermBuilder2(hist, key);
            return configure(config, builder);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public LegDistributionTermBuilder2 configure(HierarchicalConfiguration config, LegDistributionTermBuilder2 builder) {
        /** debug interval */
        builder.debugInterval((long) config.getDouble(DEBUG_INTERVAL, 0));

        /** error function */
        double exponent = config.getDouble(ERROR_EXPONENT, 1);
        double infValue = config.getDouble(ERROR_INF_VALUE, 1);
        builder.errorFunction(new RelativeErrorFunction(exponent, infValue));

        /** logging interval */
        builder.logInterval((long) config.getDouble(LOG_INTERVAL, 0));

        /** name */
        if(config.containsKey(NAME)) {
            builder.name(config.getString(NAME));
        }

        /** reset interval */
        builder.resetInterval((long) config.getDouble(RESET_INTERVAL, Double.MAX_VALUE));

        /** start iteration */
        builder.startIteration((long) config.getDouble(START_ITERATION, 0));

        /** thetas */
        builder.thetaMax(config.getDouble(THETA_MAX, 1));
        builder.thetaMin(config.getDouble(THETA_MIN, 1));
        builder.thetaFactor(config.getDouble(THETA_FACTOR, 10));
        builder.thetaInterval((long) config.getDouble(THETA_INTERVAL, 1E7));
        builder.thetaThreshold(config.getDouble(THETA_THRESHOLD, 0.005));

        return builder;
    }
}
