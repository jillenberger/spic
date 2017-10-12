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

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.ActivityLocationLayer;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.matrix.*;
import gnu.trove.list.array.TDoubleArrayList;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class MatrixAnalyzer implements AnalyzerTask<Collection<? extends Person>> {

    private static final Logger logger = Logger.getLogger(MatrixAnalyzer.class);

    private static final String KEY = "matrix";
    private final String matrixName;
    private final DefaultMatrixBuilder matrixBuilder;
    private NumericMatrix refMatrix;
    private Predicate<Segment> predicate;

    private FileIOContext ioContext;

    private HistogramWriter histogramWriter;

    private ODPredicate<String, Double> odPredicate;

    private double volumeThreshold = 0;

    private boolean useWeights;

//    private

    public MatrixAnalyzer(ActivityLocationLayer facilities, ZoneCollection zones, NumericMatrix refMatrix, String name, String layerName) {
        this.refMatrix = refMatrix;
        this.matrixName = name;

        System.err.println("Deprecated code!");
        System.exit(-1);
        matrixBuilder = new DefaultMatrixBuilder(null, null);
        throw new RuntimeException("Deprected code?");
    }

    public void setLegPredicate(Predicate<Segment> predicate) {
        this.predicate = predicate;
    }

    public void setODPredicate(ODPredicate<String, Double> odPredicate) {
        this.odPredicate = odPredicate;
    }

    public void setVolumeThreshold(double threshold) {
        this.volumeThreshold = threshold;
    }

    public void setUseWeights(boolean useWeights) {
        this.useWeights = useWeights;
    }

    public void setFileIOContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
        if (ioContext != null)
            histogramWriter = new HistogramWriter(ioContext, new PassThroughDiscretizerBuilder(new LinearDiscretizer(0.05), "linear"));
        else
            histogramWriter = null;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        matrixBuilder.setUseWeights(useWeights);
        matrixBuilder.setLegPredicate(predicate);
        NumericMatrix simMatrix = matrixBuilder.build(persons);

        if (odPredicate != null) {
            NumericMatrix tmpMatrix = new NumericMatrix();
            MatrixOperations.subMatrix(odPredicate, simMatrix, tmpMatrix);
            simMatrix = tmpMatrix;
        }

        double simTotal = MatrixOperations.sum(simMatrix);

        NumericMatrix tmpRefMatrix = refMatrix;
        if (odPredicate != null) {
            tmpRefMatrix = (NumericMatrix) MatrixOperations.subMatrix(odPredicate, tmpRefMatrix, new NumericMatrix());
        }

        double refTotal = MatrixOperations.sum(tmpRefMatrix);

        if(volumeThreshold > 0) {
            ODPredicate volPredicate = new VolumePredicate(volumeThreshold);
            tmpRefMatrix = (NumericMatrix) MatrixOperations.subMatrix(volPredicate, tmpRefMatrix, new NumericMatrix());
        }

        logger.debug(String.format("Normalization factor (%s): %s.", matrixName, simTotal/refTotal));
        MatrixOperations.applyFactor(tmpRefMatrix, simTotal / refTotal);

        NumericMatrix errMatrix = new NumericMatrix();
        MatrixOperations.errorMatrix(tmpRefMatrix, simMatrix, errMatrix);

        double[] errors = org.matsim.contrib.common.collections.CollectionUtils.toNativeArray(errMatrix.values(), true, true, true);

        String name = String.format("%s.%s.err", KEY, matrixName);
        StatsContainer container = new StatsContainer(name, errors);
        containers.add(container);

        if (histogramWriter != null)
            histogramWriter.writeHistograms(errors, name);

        if (ioContext != null) {
            try {
                /*
                write scatter plot
                */
                Set<String> keys = tmpRefMatrix.keys();
                keys.addAll(simMatrix.keys());

                logger.debug(String.format("Compared %s od relations.", keys.size()));

                TDoubleArrayList refVals = new TDoubleArrayList();
                TDoubleArrayList simVals = new TDoubleArrayList();
                for (String i : keys) {
                    for (String j : keys) {
                        Double refVol = tmpRefMatrix.get(i, j);
                        Double simVol = simMatrix.get(i, j);

                        if (refVol != null || simVol != null) {
                            if (refVol == null) refVol = 0.0;
                            if (simVol == null) simVol = 0.0;
                            refVals.add(refVol);
                            simVals.add(simVol);
                        }
                    }
                }

                StatsWriter.writeScatterPlot(refVals, simVals, matrixName, "simulation", String.format
                        ("%s/matrix.%s.scatter.txt", ioContext.getPath(), matrixName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        container = new StatsContainer(name, errors);
        containers.add(container);
    }
}
