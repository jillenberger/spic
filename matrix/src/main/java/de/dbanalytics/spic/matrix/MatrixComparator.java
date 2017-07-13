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
import de.dbanalytics.spic.analysis.Predicate;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class MatrixComparator implements AnalyzerTask<Collection<? extends Person>> {

    private static final Logger logger = Logger.getLogger(MatrixComparator.class);

    private final MatrixBuilder builder;

    private final NumericMatrix refMatrix;

    private ODPredicate<String, Double> normPredicate;

    private double volumeThreshold;

    private AnalyzerTask<Pair<NumericMatrix, NumericMatrix>> tasks;

    public MatrixComparator(NumericMatrix refMatrix, MatrixBuilder builder, AnalyzerTask<Pair<NumericMatrix, NumericMatrix>> tasks) {
        this.refMatrix = refMatrix;
        this.tasks = tasks;
        this.builder = builder;
        volumeThreshold = 0;
    }

    public void setLegPredicate(Predicate<Segment> predicate) {
        builder.setLegPredicate(predicate);
    }

    public void setUseWeights(boolean useWeights) {
        builder.setUseWeights(useWeights);
    }

    public void setNormPredicate(ODPredicate<String, Double> normPredicate) {
        this.normPredicate = normPredicate;
    }

    public void setVolumeThreshold(double threshold) {
        this.volumeThreshold = threshold;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        NumericMatrix simMatrix = builder.build(persons);
        NumericMatrix tmpSimMatrix = simMatrix;
        if (normPredicate != null) {
            tmpSimMatrix = (NumericMatrix) MatrixOperations.subMatrix(normPredicate, simMatrix, new NumericMatrix());
        }

        double simTotal = MatrixOperations.sum(tmpSimMatrix);

        NumericMatrix tmpRefMatrix = refMatrix;
        if (normPredicate != null) {
            tmpRefMatrix = (NumericMatrix) MatrixOperations.subMatrix(normPredicate, refMatrix, new NumericMatrix());
        }

        double refTotal = MatrixOperations.sum(tmpRefMatrix);

        if(volumeThreshold > 0) {
            ODPredicate volPredicate = new VolumePredicate(volumeThreshold);
            // TODO: this requires further considerations...
            tmpRefMatrix = (NumericMatrix) MatrixOperations.subMatrix(volPredicate, tmpRefMatrix, new NumericMatrix());
        }

        double normFactor = refTotal/simTotal;
        MatrixOperations.applyFactor(simMatrix, normFactor);
        logger.debug(String.format("Normalization factor: %s.", normFactor));

        tasks.analyze(new ImmutablePair<>(tmpRefMatrix, simMatrix), containers);
    }
}
