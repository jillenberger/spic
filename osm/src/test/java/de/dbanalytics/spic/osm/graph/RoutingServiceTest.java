/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.osm.graph;

import gnu.trove.list.array.TLongArrayList;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * @author johannes
 */
public class RoutingServiceTest extends TestCase {

    public void testPath() {
        String osmFile = "/Users/johannes/Desktop/bockenheim.osm";
        String tmpDir = "/Users/johannes/Desktop/bockenheim-gh";

        RoutingService router = new RoutingService(osmFile, tmpDir);

        double fromLat = 50.1209992;
        double fromLon = 8.6439189;
        double toLat = 50.1201303;
        double toLon = 8.6421616;

        TLongArrayList expected = new TLongArrayList();
        expected.add(603974);
        expected.add(603971);
        expected.add(779165430);
        expected.add(132498080);
        expected.add(2516558319L);
        expected.add(571377284);
        expected.add(132498089);
        expected.add(132498193);
        expected.add(132498203);
        expected.add(2516558318L);
        expected.add(31268141);

        RoutingResult result = router.query(fromLat, fromLon, toLat, toLon);

        System.out.println(result.getPath().toString());

        Assert.assertArrayEquals(expected.toArray(), result.getPath().toArray());


    }
}
