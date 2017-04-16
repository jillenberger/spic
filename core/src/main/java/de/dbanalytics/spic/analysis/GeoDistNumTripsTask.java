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

import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.matsim.contrib.common.stats.Correlations;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class GeoDistNumTripsTask implements AnalyzerTask<Collection<? extends Person>> {

    private final Predicate<Segment> predicate;

    private final FileIOContext ioContext;

    public GeoDistNumTripsTask(FileIOContext ioContext, Predicate<Segment> predicate) {
        this.ioContext = ioContext;
        this.predicate = predicate;
    }
    @Override
    public void analyze(Collection<? extends Person> object, List<StatsContainer> containers) {
        TDoubleArrayList numsTrips = new TDoubleArrayList();
        TDoubleArrayList dists = new TDoubleArrayList();

        for(Person p : object) {
            for(Episode e : p.getEpisodes()) {
                int trips = 0;
                double sum = 0;
                for(Segment leg : e.getLegs()) {
                    if(predicate == null || predicate.test(leg)) {
                        String value = leg.getAttribute(CommonKeys.LEG_GEO_DISTANCE);
                        if(value != null) {
                            sum += Double.parseDouble(value);
                            trips++;
                        }
                    }
                }

                numsTrips.add(trips);
                dists.add(sum/(double)trips);
            }
        }

        TDoubleDoubleHashMap correl = Correlations.mean(numsTrips.toArray(), dists.toArray());
        try {
            StatsWriter.writeHistogram(correl, "trips", "distance", String.format("%s/geoDistNumTrips.txt", ioContext.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
