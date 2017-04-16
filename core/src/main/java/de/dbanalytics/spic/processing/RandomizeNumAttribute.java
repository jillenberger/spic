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

package de.dbanalytics.spic.processing;

import de.dbanalytics.spic.data.Segment;

import java.util.Random;

/**
 * Created by johannesillenberger on 06.04.17.
 */
public class RandomizeNumAttribute implements SegmentTask {

    private final String key;

    private final double range;

    private final Random random;

    public RandomizeNumAttribute(String key, double range, Random random) {
        this.key = key;
        this.range = range;
        this.random = random;
    }

    @Override
    public void apply(Segment segment) {
        String val = segment.getAttribute(key);
        if(val != null) {
            double numVal = Double.parseDouble(val);
            double offset = (random.nextDouble() - 0.5) * 2 * range;
            numVal += offset;
            segment.setAttribute(key, String.valueOf(numVal));
        }
    }
}
