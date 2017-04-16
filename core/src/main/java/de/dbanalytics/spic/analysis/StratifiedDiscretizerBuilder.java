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

package de.dbanalytics.spic.analysis;

import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.FixedSampleSizeDiscretizer;

/**
 * @author johannes
 */
public class StratifiedDiscretizerBuilder implements DiscretizerBuilder {

    public static final String DEFAULT_NAME = "stratified";

    private final int numBins;

    private final int minSize;

    private final String name;

    public StratifiedDiscretizerBuilder(int numBins, int minSize) {
        this(numBins, minSize, DEFAULT_NAME);
    }

    public StratifiedDiscretizerBuilder(int numBins, int minSize, String name) {
        this.numBins = numBins;
        this.minSize = minSize;
        this.name = name;
    }

    @Override
    public Discretizer build(double[] values) {
        return FixedSampleSizeDiscretizer.create(values, minSize, numBins);
    }

    @Override
    public String getName() {
        return name;
    }
}
