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
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.sim.HistogramBuilder;
import gnu.trove.map.TDoubleDoubleMap;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import gnu.trove.set.TDoubleSet;
import gnu.trove.set.hash.TDoubleHashSet;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class HistogramComparator implements AnalyzerTask<Collection<? extends Person>>{

    private final String dimension;

    private final TDoubleDoubleMap refHist;

    private final HistogramBuilder builder;

    private FileIOContext ioContext;

    public HistogramComparator(TDoubleDoubleMap refHist, HistogramBuilder builder, String dimension) {
        this.refHist = refHist;
        this.builder = builder;
        this.dimension = dimension;

        Histogram.normalize((TDoubleDoubleHashMap) refHist);
    }

    public void setFileIoContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        TDoubleDoubleMap simHist = builder.build(persons);
        Histogram.normalize((TDoubleDoubleHashMap) simHist);

        TDoubleSet keySet = new TDoubleHashSet(refHist.keySet());
        keySet.addAll(simHist.keySet());

        TDoubleDoubleMap errHist = new TDoubleDoubleHashMap();
        double[] keyArray = keySet.toArray();
        for(double bin : keyArray) {
            double err = (simHist.get(bin) - refHist.get(bin))/refHist.get(bin);
            errHist.put(bin, err);
        }

        containers.add(new StatsContainer(String.format("%s.errHist", dimension), errHist.values()));

        if(ioContext != null) {
            try {
                StatsWriter.writeHistogram(
                        (TDoubleDoubleHashMap) errHist,
                        "bin",
                        "error",
                        String.format("%s/%s.errHist.txt", ioContext.getPath(), dimension));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
