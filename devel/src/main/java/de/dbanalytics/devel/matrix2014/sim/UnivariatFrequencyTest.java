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

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainPerson;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;
import junit.framework.TestCase;
import org.junit.Assert;
import org.matsim.contrib.common.stats.LinearDiscretizer;

import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 */
public class UnivariatFrequencyTest extends TestCase {

    public void test() {
        Person ref1 = new PlainPerson("1");
        ref1.setAttribute("attribute", "1");

        Person ref2 = new PlainPerson("2");
        ref2.setAttribute("attribute", "1");

        Person ref3 = new PlainPerson("3");
        ref3.setAttribute("attribute", "2");

        Set<Person> refPersons = new HashSet<>();
        refPersons.add(ref1);
        refPersons.add(ref2);
        refPersons.add(ref3);

        Person sim1 = new PlainPerson("1");
        sim1.setAttribute("attribute", "2");
        CachedPerson c1 = new CachedPerson(sim1);

        Person sim2 = new PlainPerson("2");
        sim2.setAttribute("attribute", "2");
        CachedPerson c2 = new CachedPerson(sim2);

        Person sim3 = new PlainPerson("3");
        sim3.setAttribute("attribute", "3");
        CachedPerson c3 = new CachedPerson(sim3);

        Set<CachedPerson> cachedPersons = new HashSet<>();
        cachedPersons.add(c1);
        cachedPersons.add(c2);
        cachedPersons.add(c3);

        Object dataKey = Converters.register("attribute", DoubleConverter.getInstance());

        UnivariatFrequency uf = new UnivariatFrequency(refPersons, cachedPersons, "attribute", new LinearDiscretizer
                (1.0));

        double binCount = 4;
        Assert.assertEquals(3.0/binCount, uf.evaluate(null), 0.0);

        c1.setData(dataKey, 1.0);
        uf.update(dataKey, 2.0, 1.0, c1);
        Assert.assertEquals(1.5/binCount, uf.evaluate(null), 0.0);

        c3.setData(dataKey, 1.0);
        uf.update(dataKey, 3.0, 1.0, c3);
        Assert.assertEquals(0.0/binCount, uf.evaluate(null), 0.0);

    }
}
