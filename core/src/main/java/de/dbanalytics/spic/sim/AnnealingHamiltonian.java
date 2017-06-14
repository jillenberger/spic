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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author johannes
 */
public class AnnealingHamiltonian implements Hamiltonian, MarkovEngineListener {

    private final static Logger logger = Logger.getLogger(AnnealingHamiltonian.class);

    private final Hamiltonian delegate;

    private final double theta_min;

    private final double theta_max;

    private double theta_factor = 10;

    private long delta_interval = (long) 1e7;

    private double delta_threshold = 0.005;

    private long startIteration = 0;

    private double theta = 0.0;

    private double h_old = Double.MAX_VALUE;

    private long iteration;

    public AnnealingHamiltonian(Hamiltonian delegate, double theta_min, double theta_max) {
        this.delegate = delegate;
        this.theta_min = theta_min;
        this.theta_max = theta_max;

        theta = theta_min;
    }

    public void setThetaFactor(double factor) {
        this.theta_factor = factor;
    }

    public void setThetaInterval(long interval) {
        this.delta_interval = interval;
    }

    public void setThetaThreshold(double threshold) {
        this.delta_threshold = threshold;
    }

    public long getStartIteration() {
        return startIteration;
    }

    public void setStartIteration(long iteration) {
        this.startIteration = iteration;
    }

    @Override
    public double evaluate(Collection<CachedPerson> population) {
        if(iteration >= startIteration) return theta * delegate.evaluate(population);
        else return 0.0;
    }

    private void update(Collection<CachedPerson> population) {
        double h_new = delegate.evaluate(population);
        double delta = h_old - h_new;

        if (delta < delta_threshold) {
            double thetaNew = theta * theta_factor;
            thetaNew = Math.max(thetaNew, theta_min);
            thetaNew = Math.min(thetaNew, theta_max);

            if (thetaNew != theta) logger.trace(String.format("Theta update triggered: %s", thetaNew));

            theta = thetaNew;
        }

        h_old = h_new;
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        if (iteration >= startIteration && iteration % delta_interval == 0) {
            update(population);
        }

        iteration++;
    }
}
