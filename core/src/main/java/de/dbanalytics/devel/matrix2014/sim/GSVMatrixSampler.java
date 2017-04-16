/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2016 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package de.dbanalytics.devel.matrix2014.sim;

import de.dbanalytics.devel.matrix2014.analysis.MatrixBuilder;
import de.dbanalytics.devel.matrix2014.gis.ActivityLocationLayer;
import de.dbanalytics.devel.matrix2014.gis.ActivityLocationLayerLoader;
import de.dbanalytics.devel.matrix2014.matrix.DefaultMatrixBuilder;
import de.dbanalytics.devel.matrix2014.matrix.io.GSVMatrixWriter;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.gis.DataPool;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneData;
import de.dbanalytics.spic.gis.ZoneDataLoader;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.sim.MarkovEngineListener;
import de.dbanalytics.spic.sim.data.CachedPerson;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class GSVMatrixSampler implements AnalyzerTask<Collection<? extends Person>>, MarkovEngineListener {

    private final Collection<Pair<MatrixSampler, Map<String, String>>> builders;

    private final FileIOContext ioContext;

    public GSVMatrixSampler(Collection<? extends Person> persons,
                            DataPool dataPool,
                            String layerName,
                            Random random,
                            long start,
                            long step,
                            FileIOContext ioContext) {

        this.ioContext = ioContext;
        ActivityLocationLayer activityLocationLayer = (ActivityLocationLayer) dataPool.get(ActivityLocationLayerLoader.KEY);
        ZoneData zoneData = (ZoneData) dataPool.get(ZoneDataLoader.KEY);
        ZoneCollection zones = zoneData.getLayer(layerName);

        Collector<String> collector = new LegCollector<>(new AttributeProvider<Segment>(CommonKeys.LEG_PURPOSE));
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
                    activityLocationLayer,
                    zones,
                    start,
                    step));

            builders.add(buildPair(
                    modePredicate,
                    purpose,
                    DirectionPredicate.RETURN,
                    random,
                    activityLocationLayer,
                    zones,
                    start,
                    step));
        }
    }

    private Pair<MatrixSampler, Map<String, String>> buildPair(Predicate<Segment> modePredicate,
                                                               String purpose,
                                                               String direction,
                                                               Random random,
                                                               ActivityLocationLayer locarions,
                                                               ZoneCollection zones,
                                                               long start,
                                                               long step) {

        Predicate<Segment> purposePredicate = new LegAttributePredicate(CommonKeys.LEG_PURPOSE, purpose);
        Predicate<Segment> outPredicate = new DirectionPredicate(direction, true, random);

        Predicate<Segment> andPredicate = PredicateAndComposite.create(
                purposePredicate,
                outPredicate,
                modePredicate);

        MatrixSampler builder = new MatrixSampler(new DefaultMatrixBuilder(locarions, zones), start, step);
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