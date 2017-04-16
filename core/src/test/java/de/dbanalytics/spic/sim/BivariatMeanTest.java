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

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainPerson;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.matsim.contrib.common.stats.LinearDiscretizer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 */
public class BivariatMeanTest extends TestCase {

    public void test() {
        Person refPerson1 = new PlainPerson("1");
        refPerson1.setAttribute("attribute1", "1");
        refPerson1.setAttribute("attribute2", "1");

        Person refPerson2 = new PlainPerson("2");
        refPerson2.setAttribute("attribute1", "3");
        refPerson2.setAttribute("attribute2", "1");

        Set<Person> refPersons = new HashSet<>();
        refPersons.add(refPerson1);
        refPersons.add(refPerson2);

        Person simPerson1 = new PlainPerson("1");
        simPerson1.setAttribute("attribute1", "1");
        simPerson1.setAttribute("attribute2", "0");

        Person simPerson2 = new PlainPerson("2");
        simPerson2.setAttribute("attribute1", "3");
        simPerson2.setAttribute("attribute2", "5");

        CachedPerson cachedPerson1 = new CachedPerson(simPerson1);
        CachedPerson cachedPerson2 = new CachedPerson(simPerson2);

        Set<CachedPerson> simPersons = new HashSet<>();
        simPersons.add(cachedPerson1);
        simPersons.add(cachedPerson2);

        Object dataKey1 = Converters.register("attribute1", DoubleConverter.getInstance());
        Object dataKey2 = Converters.register("attribute2", DoubleConverter.getInstance());

        BivariatMean mm = new BivariatMean(refPersons, simPersons, "attribute1", "attribute2", new
                LinearDiscretizer(1));

        double binCount = 4.0;

        Assert.assertEquals(5.0/binCount, mm.evaluate(null));

        cachedPerson1.setData(dataKey2, 1.0);
        mm.onChange(dataKey2, 0.0, 1.0, cachedPerson1);
        Assert.assertEquals(4.0/binCount, mm.evaluate(null));

        cachedPerson1.setData(dataKey1, 0.0);
        mm.onChange(dataKey1, 1.0, 0.0, cachedPerson1);
        Assert.assertEquals(4.0/binCount, mm.evaluate(null));

        cachedPerson1.setData(dataKey2, 0.0);
        mm.onChange(dataKey2, 1.0, 0.0, cachedPerson1);
        Assert.assertEquals(4.0/binCount, mm.evaluate(null));

        cachedPerson1.setData(dataKey1, 3.0);
        mm.onChange(dataKey1, 0.0, 3.0, cachedPerson1);
        Assert.assertEquals(1.5/binCount, mm.evaluate(null));
    }


}
