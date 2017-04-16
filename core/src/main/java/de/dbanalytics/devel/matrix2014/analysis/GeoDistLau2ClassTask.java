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

package de.dbanalytics.devel.matrix2014.analysis;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.Attributable;
import de.dbanalytics.spic.data.CommonKeys;
import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.source.mid2008.MiDKeys;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.matsim.contrib.common.stats.Correlations;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class GeoDistLau2ClassTask implements AnalyzerTask<Collection<? extends Person>> {

    private final FileIOContext ioContext;

    public GeoDistLau2ClassTask(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        TDoubleArrayList xVals = new TDoubleArrayList();
        TDoubleArrayList yVals = new TDoubleArrayList();

        for (Person person : persons) {
            String xStr = person.getAttribute(MiDKeys.PERSON_LAU2_CLASS);
            for (Episode plan : person.getEpisodes()) {
                for (Attributable leg : plan.getLegs()) {

                    String yStr = leg.getAttribute(CommonKeys.LEG_GEO_DISTANCE);

                    if (xStr != null && yStr != null) {
                        xVals.add(Double.parseDouble(xStr));
                        yVals.add(Double.parseDouble(yStr));
                    }
                }
            }
        }

        try {
            String filename = String.format("%s/munic.dist.mean.txt", ioContext.getPath());
            double[] x = xVals.toArray();
            double[] y = yVals.toArray();
            TDoubleDoubleHashMap corr = Correlations.mean(x, y);
            StatsWriter.writeHistogram(corr, "munic", "distance", filename);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
