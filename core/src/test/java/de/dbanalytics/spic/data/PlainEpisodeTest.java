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

package de.dbanalytics.spic.data;


import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author johannes
 */
public class PlainEpisodeTest extends TestCase {

    public void testNavigation() {
        Person person = new PlainPerson("1");
        Episode episode = new PlainEpisode();

        person.addEpisode(episode);

        Segment act0 = new PlainSegment();
        Segment leg0 = new PlainSegment();
        Segment act1 = new PlainSegment();
        Segment leg1 = new PlainSegment();
        Segment act2 = new PlainSegment();

        episode.addActivity(act0);
        episode.addActivity(act1);
        episode.addActivity(act2);

        episode.addLeg(leg0);
        episode.addLeg(leg1);

        Assert.assertEquals(episode.getPerson(), person);


        Assert.assertEquals(act0.previous(), null);
        Assert.assertEquals(act0.next(), leg0);
        Assert.assertEquals(leg0.previous(), act0);
        Assert.assertEquals(leg0.next(), act1);
        Assert.assertEquals(act1.previous(), leg0);
        Assert.assertEquals(act1.next(), leg1);
        Assert.assertEquals(leg1.previous(), act1);
        Assert.assertEquals(leg1.next(), act2);
        Assert.assertEquals(act2.previous(), leg1);
        Assert.assertEquals(act2.next(), null);
    }

//    @Rule
//    private final ExpectedException exception = ExpectedException.none();
//
//    @Test
//    public void testDuplicateEntries() {
//        Episode episode = new PlainEpisode();
//
//        Segment act = new PlainSegment();
//
//        episode.addActivity(act);
//        exception.expect(IllegalArgumentException.class);
//        episode.addActivity(act);
//    }
}
