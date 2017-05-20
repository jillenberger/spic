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

package de.dbanalytics.spic.mid2008.processing;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.processing.EpisodeTask;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author johannes
 */
public class SortLegsTask implements EpisodeTask {

    private final String key;

    private final Comparator<String> comparator;

    public SortLegsTask(String key) {
        this.key = key;
        this.comparator = null;
    }

    public SortLegsTask(String key, Comparator<String> comparator) {
        this.key = key;
        this.comparator = comparator;
    }

    @Override
    public void apply(Episode episode) {
        SortedMap<String, Segment> legs = new TreeMap<>();
        if(comparator != null) legs = new TreeMap<>(comparator);

        for(Segment leg : episode.getLegs()) {
            legs.put(leg.getAttribute(key), leg);
        }

        for(Segment leg : legs.values()) {
            episode.removeLeg(leg);
        }

        for(Segment leg : legs.values()) {
            episode.addLeg(leg);
        }
    }

    public static class IntComparator implements Comparator<String> {

        @Override
        public int compare(String o1, String o2) {
            int idx1 = Integer.parseInt(o1);
            int idx2 = Integer.parseInt(o2);
            return idx1 - idx2;
        }
    }
}
