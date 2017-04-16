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

package de.dbanalytics.spic.sim.data;

import de.dbanalytics.spic.data.PlainSegment;
import de.dbanalytics.spic.data.Segment;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author johannes
 */
public class CachedElementTest extends TestCase {

    public void testSynchronization() {
        Segment e = new PlainSegment();

        String plainKey = "key";
        String plainValue = "1.2";
        e.setAttribute(plainKey, plainValue);

        CachedElement cache = new CachedSegment(e);
        Object objKey = new Object();
        Converters.registerWithObjectKey(plainKey, objKey, DoubleConverter.getInstance());

        Assert.assertEquals(cache.getAttribute(plainKey), plainValue);
        Assert.assertEquals(cache.getData(objKey), 1.2);
        Assert.assertEquals(cache.getAttribute("nonExistingKey"), null);
        Assert.assertEquals(cache.getData(new Object()), null);

        cache.setData(objKey, 1.3);

        Assert.assertEquals(cache.getData(objKey), 1.3);
        Assert.assertEquals(cache.getAttribute(plainKey), "1.3");

        cache.setAttribute(plainKey, "1.4");

        Assert.assertEquals(cache.getData(objKey), 1.4);

        cache.setAttribute(plainKey, null);

        Assert.assertEquals(cache.getData(objKey), null);

        cache.setAttribute(plainKey, "1.6");

        Assert.assertEquals(cache.getData(objKey), 1.6);

        cache.setData(objKey, null);

        Assert.assertEquals(cache.getAttribute(plainKey), "1.6");
    }
}
