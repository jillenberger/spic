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

package de.dbanalytics.spic.matrix;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.HistogramWriter;
import de.dbanalytics.spic.analysis.StatsContainer;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class MatrixVolumeCompare implements AnalyzerTask<Pair<NumericMatrix, NumericMatrix>> {

    private static final Logger logger = Logger.getLogger(MatrixVolumeCompare.class);

    private final String dimension;

    private FileIOContext ioContext;

    private HistogramWriter histogramWriter;

    public MatrixVolumeCompare(String dimension) {
        this.dimension = dimension;
    }

    public void setIoContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    public void setHistogramWriter(HistogramWriter histogramWriter) {
        this.histogramWriter = histogramWriter;
    }

    @Override
    public void analyze(Pair<NumericMatrix, NumericMatrix> matrices, List<StatsContainer> containers) {
        NumericMatrix refMatrix = matrices.getLeft();
        NumericMatrix simMatrix = matrices.getRight();

        NumericMatrix errMatrix = new NumericMatrix();
        MatrixOperations.errorMatrix(refMatrix, simMatrix, errMatrix);

        double[] errors = org.matsim.contrib.common.collections.CollectionUtils.toNativeArray(errMatrix.values());
        logger.debug(String.format("Compared %s od relations.", errors.length));

        String name = String.format("%s.err", dimension);
        StatsContainer container = new StatsContainer(name, errors);
        containers.add(container);

        if (histogramWriter != null)
            histogramWriter.writeHistograms(errors, name);

        if (ioContext != null) {
            try {
                /*
                write scatter plot
                */
                Set<String> keys = refMatrix.keys();
                keys.addAll(simMatrix.keys());

                TDoubleArrayList refVals = new TDoubleArrayList();
                TDoubleArrayList simVals = new TDoubleArrayList();
                for (String i : keys) {
                    for (String j : keys) {
                        Double refVol = refMatrix.get(i, j);
                        Double simVol = simMatrix.get(i, j);

                        if (refVol != null || simVol != null) {
                            if (refVol == null) refVol = 0.0;
                            if (simVol == null) simVol = 0.0;
                            refVals.add(refVol);
                            simVals.add(simVol);
                        }
                    }
                }

                StatsWriter.writeScatterPlot(refVals, simVals, dimension, "simulation", String.format
                        ("%s/%s.scatter.txt", ioContext.getPath(), dimension));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
