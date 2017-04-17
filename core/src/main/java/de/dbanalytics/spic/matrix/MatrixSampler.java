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

package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.sim.MarkovEngineListener;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Set;

/**
 * @author johannes
 */
public class MatrixSampler implements MatrixBuilder, MarkovEngineListener {

    private static final Logger logger = Logger.getLogger(MatrixSampler.class);

    private final long start;

    private final long step;

    private long iteration;

    private final NumericMatrix sumMatrix = new NumericMatrix();

    private NumericMatrix avrMatrix;

    private int sampleSize;

    private MatrixBuilder builder;

    public MatrixSampler(MatrixBuilder builder, long start, long step) {
        this.builder = builder;
        this.start = start;
        this.step = step;
        avrMatrix = new NumericMatrix();

    }

    public void drawSample(Collection<? extends Person> persons) {
        NumericMatrix sample = builder.build(persons);

        Set<String> keys = sample.keys();
        for(String i : keys) {
            for(String j : keys) {
                Double vol = sample.get(i, j);
                if(vol != null) {
                    sumMatrix.add(i, j, vol);
                }
            }
        }

        sampleSize++;

        avrMatrix = new NumericMatrix();
        keys = sumMatrix.keys();
        for(String i : keys) {
            for(String j : keys) {
                Double vol = sumMatrix.get(i, j);
                if(vol != null) {
                    avrMatrix.set(i, j, vol/(double)sampleSize);
                }
            }
        }
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        iteration++;

        if(iteration >= start && iteration % step == 0) {
            logger.debug(String.format("Drawing matrix sample. Current sample size = %s.", sampleSize));
            drawSample(population);
            logger.debug("Done drawing matrix sample.");
        }
    }

    @Override
    public void setLegPredicate(Predicate<Segment> predicate) {
        builder.setLegPredicate(predicate);
    }

    @Override
    public void setUseWeights(boolean useWeights) {
        builder.setUseWeights(useWeights);
    }

    @Override
    public NumericMatrix build(Collection<? extends Person> population) {
        return avrMatrix;
    }
}
