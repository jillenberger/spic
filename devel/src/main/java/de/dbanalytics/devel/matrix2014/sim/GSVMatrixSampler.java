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

import de.dbanalytics.devel.matrix2014.matrix.io.GSVMatrixWriter;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.matrix.DefaultMatrixBuilder;
import de.dbanalytics.spic.matrix.MatrixBuilder;
import de.dbanalytics.spic.matrix.MatrixSampler;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.sim.McmcSimulationObserver;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class GSVMatrixSampler implements AnalyzerTask<Collection<? extends Person>>, McmcSimulationObserver {

    private final Collection<Pair<MatrixSampler, Map<String, String>>> builders;

    private final FileIOContext ioContext;

    public GSVMatrixSampler(Collection<? extends Person> persons,
                            PlaceIndex placeIndex,
                            Set<Feature> features,
                            Random random,
                            long start,
                            long step,
                            FileIOContext ioContext) {

        this.ioContext = ioContext;

        ZoneIndex zones = new ZoneIndex(features);

        Collector<String> collector = new LegCollector<>(new AttributeProvider<>(CommonKeys.LEG_PURPOSE));
        Set<String> purposes = new HashSet<>(collector.collect(persons));
        purposes.remove(null);

        builders = new ArrayList<>();

        Predicate<Segment> modePredicate = new LegAttributePredicate(CommonKeys.LEG_MODE, CommonValues.LEG_MODE_CAR);

        for(String purpose : purposes) {
            builders.add(buildPair(
                    modePredicate,
                    purpose,
                    DirectionPredicate.OUTWARD,
                    random,
                    placeIndex,
                    zones,
                    start,
                    step));

            builders.add(buildPair(
                    modePredicate,
                    purpose,
                    DirectionPredicate.RETURN,
                    random,
                    placeIndex,
                    zones,
                    start,
                    step));
        }
    }

    private Pair<MatrixSampler, Map<String, String>> buildPair(Predicate<Segment> modePredicate,
                                                               String purpose,
                                                               String direction,
                                                               Random random,
                                                               PlaceIndex places,
                                                               ZoneIndex zones,
                                                               long start,
                                                               long step) {

        Predicate<Segment> purposePredicate = new LegAttributePredicate(CommonKeys.LEG_PURPOSE, purpose);
        Predicate<Segment> outPredicate = new DirectionPredicate(direction, true, random);

        Predicate<Segment> andPredicate = PredicateAndComposite.create(
                purposePredicate,
                outPredicate,
                modePredicate);

        MatrixSampler builder = new MatrixSampler(new DefaultMatrixBuilder(places, zones), start, step);
        builder.setLegPredicate(andPredicate);
        builder.setUseWeights(true);

        Map<String, String> dimensions = new HashMap<>();
        dimensions.put(GSVMatrixWriter.MODE_KEY, CommonValues.LEG_MODE_CAR);
        dimensions.put(GSVMatrixWriter.PURPOSE_KEY, purpose);
        dimensions.put(GSVMatrixWriter.DIRECTION_KEY, direction);

        return new ImmutablePair<>(builder, dimensions);
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        List<Pair<NumericMatrix, Map<String, String>>> matrices = new ArrayList<>(builders.size());

        for(Pair<MatrixSampler, Map<String, String>> pair : builders) {
            MatrixBuilder builder = pair.getLeft();
            Map<String, String> dimensions = pair.getRight();

            NumericMatrix m = builder.build(persons);

            matrices.add(new ImmutablePair<>(m, dimensions));
        }

        try {
            GSVMatrixWriter.write(matrices, String.format("%s/gsv-matrix.txt.gz", ioContext.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
        for(Pair<MatrixSampler, Map<String, String>> builder : builders) {
            builder.getLeft().afterStep(population, mutations, accepted);
        }
    }
}
