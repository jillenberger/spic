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
package de.dbanalytics.devel.matrix2014.sim.run;

import de.dbanalytics.devel.matrix2014.config.MatrixAnalyzerConfigurator;
import de.dbanalytics.devel.matrix2014.gis.ZoneCollection;
import de.dbanalytics.devel.matrix2014.gis.ZoneData;
import de.dbanalytics.devel.matrix2014.gis.ZoneDataLoader;
import de.dbanalytics.spic.analysis.AnalyzerTaskComposite;
import de.dbanalytics.spic.analysis.AnalyzerTaskGroup;
import de.dbanalytics.spic.analysis.ConcurrentAnalyzerTask;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.matrix.*;
import de.dbanalytics.spic.sim.PopulationWriter;
import de.dbanalytics.spic.spic2matsim.PlaceConverter;
import org.matsim.core.config.Config;

import java.util.Collection;
import java.util.Set;

/**
 * @author jillenberger
 */
public class ExtendedAnalyzerBuilder {

    public static void build(Simulator engine, Config config) {
        AnalyzerTaskComposite<Collection<? extends Person>> task = engine.getAnalyzerTasks();

        ZoneData zoneData = (ZoneData) engine.getDataPool().get(ZoneDataLoader.KEY);
//        ActivityLocationLayer locations = (ActivityLocationLayer) engine.getDataPool().get(ActivityLocationLayerLoader.KEY);

        DefaultMatrixBuilderFactory matrixBuilderFactory = new DefaultMatrixBuilderFactory();
        /*
        matrix comparators
         */
        ConcurrentAnalyzerTask<Collection<? extends Person>> matrixTasks = new ConcurrentAnalyzerTask<>();
        MatrixComparator mAnalyzer = (MatrixComparator) new MatrixAnalyzerConfigurator(
                config.getModule("matrixAnalyzerITP"),
                engine.getDataPool(),
                matrixBuilderFactory,
                engine.getIOContext()).load();
        mAnalyzer.setLegPredicate(engine.getLegPredicate());
        mAnalyzer.setUseWeights(engine.getUseWeights());
        matrixTasks.addComponent(mAnalyzer);


        ZoneCollection tomtomZones = zoneData.getLayer("tomtom");

        mAnalyzer = (MatrixComparator) new MatrixAnalyzerConfigurator(
                config.getModule("matrixAnalyzerTomTom"),
                engine.getDataPool(),
                matrixBuilderFactory,
                engine.getIOContext()).load();
        mAnalyzer.setLegPredicate(engine.getLegPredicate());
        mAnalyzer.setUseWeights(engine.getUseWeights());

        ODPredicate distPredicate = new ZoneDistancePredicate(null, 100000, Double.MAX_VALUE);
        System.err.println("Deprecated code!");
        System.exit(-1);

        mAnalyzer.setMatrixPredicate(distPredicate);

        matrixTasks.addComponent(mAnalyzer);
        /*
        matrix writer
         */
        FacilityData facilityData = (FacilityData) engine.getDataPool().get(FacilityDataLoader.KEY);
        PlaceConverter placeConverter = new PlaceConverter();
        Set<Place> places = placeConverter.convert(facilityData.getAll());
        PlaceIndex placeIndex = new PlaceIndex(places);

        MatrixBuilder tomtomBuilder = matrixBuilderFactory.create(placeIndex, null);
        tomtomBuilder.setLegPredicate(engine.getLegPredicate());
        tomtomBuilder.setUseWeights(engine.getUseWeights());

        MatrixWriter matrixWriter = new MatrixWriter(tomtomBuilder, engine.getIOContext());
        matrixTasks.addComponent(matrixWriter);

        task.addComponent(new AnalyzerTaskGroup<>(matrixTasks, engine.getIOContext(), "matrix"));
        /*
        population writer
         */
        task.addComponent(new PopulationWriter(engine.getIOContext()));


        long start = (long) Double.parseDouble(config.findParam(Simulator.MODULE_NAME, "matrixSamplingStart"));
        long step = (long) Double.parseDouble(config.findParam(Simulator.MODULE_NAME, "matrixSamplingStep"));
        MatrixSamplerFactory nuts3Sampler = new MatrixSamplerFactory(start, step, engine.getEngineListeners());

        ConcurrentAnalyzerTask<Collection<? extends Person>> samplerTasks = new ConcurrentAnalyzerTask<>();
        mAnalyzer = (MatrixComparator) new MatrixAnalyzerConfigurator(
                config.getModule("matrixAnalyzerITP"),
                engine.getDataPool(),
                nuts3Sampler,
                engine.getIOContext()).load();
        mAnalyzer.setLegPredicate(engine.getLegPredicate());
        mAnalyzer.setUseWeights(engine.getUseWeights());

        samplerTasks.addComponent(mAnalyzer);

        mAnalyzer = (MatrixComparator) new MatrixAnalyzerConfigurator(
                config.getModule("matrixAnalyzerTomTom"),
                engine.getDataPool(),
                nuts3Sampler,
                engine.getIOContext()).load();
        mAnalyzer.setLegPredicate(engine.getLegPredicate());
        mAnalyzer.setUseWeights(engine.getUseWeights());

        mAnalyzer.setMatrixPredicate(distPredicate);
        samplerTasks.addComponent(mAnalyzer);
        /*
        matrix writer
         */
        MatrixBuilder mBuilder = nuts3Sampler.create(placeIndex, null);
        mBuilder.setLegPredicate(engine.getLegPredicate());
        mBuilder.setUseWeights(engine.getUseWeights());
        matrixWriter = new MatrixWriter(mBuilder, engine.getIOContext());
        samplerTasks.addComponent(matrixWriter);

        task.addComponent(new AnalyzerTaskGroup<>(samplerTasks, engine.getIOContext(), "matrixAvr"));

//        GSVMatrixSampler gsvSampler = new GSVMatrixSampler(engine.getRefPersons(),
//                engine.getDataPool(),
//                "modena",
//                engine.getRandom(),
//                start,
//                step,
//                engine.getIOContext());
//        engine.getEngineListeners().addComponent(gsvSampler);
//        task.addComponent(gsvSampler);
    }
}
