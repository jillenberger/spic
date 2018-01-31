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

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.Hamiltonian;
import de.dbanalytics.spic.sim.McmcSimulationObserver;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author johannes
 */
public class ThetaProvider implements McmcSimulationObserver {

    private static final Logger logger = Logger.getLogger(ThetaProvider.class);

    private long iteration;

    private final long interval;

    private double theta;

    private double H_old;

    private final double minTheta;

    private final double maxTheta;

    private final double alpha;

    private final Hamiltonian hamiltonian;

    public ThetaProvider(Hamiltonian hamiltonian, double alpha, long interval) {
        this(hamiltonian, alpha, interval, 0, Double.MAX_VALUE);
    }

    public ThetaProvider(Hamiltonian hamiltonian, double alpha, long interval, double min, double max) {
        this.hamiltonian = hamiltonian;
        this.alpha = alpha;
        this.interval = interval;
        this.minTheta = min;
        this.maxTheta = max;

        H_old = Double.MAX_VALUE;
    }

    public double getTheta() {
        return theta;
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {


        if(iteration % interval == 0) {
            double H_new = hamiltonian.evaluate(population);
            double delta = H_old - H_new;
            delta = Math.max(delta, 0);

            double theta_new = minTheta + 1/(alpha * delta);
            theta_new = Math.min(theta_new, maxTheta);
            theta = Math.max(theta_new, theta);
//            theta = Math.max(theta, minTheta);

            H_old = H_new;

            logger.debug(String.format("New theta (%s): %s", hamiltonian.getClass().getSimpleName(), theta));
        }

        iteration++;
    }
}
