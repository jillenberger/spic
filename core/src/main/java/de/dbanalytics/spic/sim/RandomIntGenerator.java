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

import de.dbanalytics.spic.sim.data.CachedElement;

import java.util.Random;

/**
 * @author johannes
 */
public class RandomIntGenerator implements ValueGenerator {

    private final Random random;

    private final int offset;

    private final int factor;

    public RandomIntGenerator(Random random, int min, int max) {
        this.random = random;
        this.offset = min;
        this.factor = (max - min);
    }

    @Override
    public Object newValue(CachedElement element) {
        return new Double(Math.floor(offset + (random.nextDouble() * factor)));
    }
}
