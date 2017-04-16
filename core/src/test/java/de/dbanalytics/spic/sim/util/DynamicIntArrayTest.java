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

package de.dbanalytics.spic.sim.util;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author johannes
 */
public class DynamicIntArrayTest extends TestCase {

    public void test1() {
        DynamicIntArray array = new DynamicIntArray();

        array.set(0, 100);
        array.set(11, 111);
        array.set(4, 104);

        Assert.assertEquals(array.get(0), 100);
        Assert.assertEquals(array.get(4), 104);
        Assert.assertEquals(array.get(11), 111);
        Assert.assertEquals(array.get(1), array.naValue);

        array.set(23, 123);

        Assert.assertEquals(array.get(23), 123);
        Assert.assertEquals(array.get(4887), array.naValue);
    }

    public void test2() {
        DynamicIntArray array = new DynamicIntArray(100, 0);

        Assert.assertEquals(array.get(234), 0);
        Assert.assertEquals(array.get(0), 0);

        array.set(99, 2);
        Assert.assertEquals(array.get(99), 2);

        array.set(102, 4);
        Assert.assertEquals(array.get(102), 4);

        Assert.assertEquals(array.get(101), 0);
    }
}
