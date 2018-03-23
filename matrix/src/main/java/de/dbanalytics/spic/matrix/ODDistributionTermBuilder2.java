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

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.gis.PlaceIndex;
import de.dbanalytics.spic.gis.ZoneIndex;
import de.dbanalytics.spic.sim.*;
import org.apache.commons.lang3.tuple.Pair;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.stats.LinearDiscretizer;

import java.util.Collection;

/**
 * @author johannes
 */
public class ODDistributionTermBuilder2 implements McmcSimulationModuleBuilder<Hamiltonian> {

    private PlaceIndex placeIndex;

    private ZoneIndex zones;

    private NumericMatrix refMatrix;

    private Predicate<Segment> predicate;

    private double minDistanceThreshold = 0;

    private double maxDistanceThreshold = Double.MAX_VALUE;

    private double volumeThreshold = 0;

    private boolean useWeights = false;

    private boolean normalize = false;

    private double thetaMin = 1.0;

    private double thetaMax = 1.0;

    private double thetaFactor = 10;

    private long thetaInterval = (long) 1e7;

    private double thetaThreshold = 0.005;

    private long startIteration = 0;

    private long logInterval = 0;

    private long debugInterval = 0;

    private long resetInterval = Long.MAX_VALUE;

    private String name;

    private boolean attachAnalyzers = true;

    public ODDistributionTermBuilder2(NumericMatrix refMatrix, PlaceIndex placeIndex, ZoneIndex zones) {
        this.refMatrix = refMatrix;
        this.placeIndex = placeIndex;
        this.zones = zones;
    }

    public ODDistributionTermBuilder2 predicate(Predicate<Segment> predicate) {
        this.predicate = predicate;
        return this;
    }

    public ODDistributionTermBuilder2 minDistanceThreshold(double distanceThreshold) {
        this.minDistanceThreshold = distanceThreshold;
        return this;
    }

    public ODDistributionTermBuilder2 maxDistanceThreshold(double threshold) {
        this.maxDistanceThreshold = threshold;
        return this;
    }

    public ODDistributionTermBuilder2 volumeThreshold(double volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
        return this;
    }

    public ODDistributionTermBuilder2 useWeights(boolean useWeights) {
        this.useWeights = useWeights;
        return this;
    }

    public ODDistributionTermBuilder2 normalize(boolean normalize) {
        this.normalize = normalize;
        return this;
    }

    public ODDistributionTermBuilder2 thetaMin(double thetaMin) {
        this.thetaMin = thetaMin;
        return this;
    }

    public ODDistributionTermBuilder2 thetaMax(double thetaMax) {
        this.thetaMax = thetaMax;
        return this;
    }

    public ODDistributionTermBuilder2 thetaFactor(double thetaFactor) {
        this.thetaFactor = thetaFactor;
        return this;
    }

    public ODDistributionTermBuilder2 thetaInterval(long thetaInterval) {
        this.thetaInterval = thetaInterval;
        return this;
    }

    public ODDistributionTermBuilder2 thetaThreshold(double thetaThreshold) {
        this.thetaThreshold = thetaThreshold;
        return this;
    }

    public ODDistributionTermBuilder2 startIteration(long startIteration) {
        this.startIteration = startIteration;
        return this;
    }

    public ODDistributionTermBuilder2 logInterval(long logInterval) {
        this.logInterval = logInterval;
        return this;
    }

    public ODDistributionTermBuilder2 debugInterval(long debugInterval) {
        this.debugInterval = debugInterval;
        return this;
    }

    public ODDistributionTermBuilder2 resetInterval(long resetInterval) {
        this.resetInterval = resetInterval;
        return this;
    }

    public ODDistributionTermBuilder2 name(String name) {
        this.name = name;
        return this;
    }

    public ODDistributionTermBuilder2 attachAnalyzers(boolean flag) {
        this.attachAnalyzers = flag;
        return this;
    }

    @Override
    public Hamiltonian build(McmcSimulationContext context) {
        String dumpFilePrefix = String.format("%s/%s", context.getIoContext().getRoot(), name);
        ODCalibrator calibrator = new ODCalibrator.Builder(refMatrix, zones, placeIndex.get(), dumpFilePrefix).build();

        calibrator.setMinDistanceThreshold(minDistanceThreshold);
        calibrator.setMaxDistanceThreshold(maxDistanceThreshold);
        calibrator.setVolumeThreshold(volumeThreshold);
        calibrator.setResetInterval(resetInterval);
        calibrator.setUseWeights(useWeights);
        calibrator.setPredicate(predicate);
        calibrator.setNormalize(normalize);


        /** Add to facility attribute change listener **/
        context.getAttributeMediator().attach(calibrator);

        /** Wrap in an annealing hamiltonian */
        AnnealingHamiltonian aTerm = new AnnealingHamiltonian(calibrator, thetaMin, thetaMax);
        aTerm.setStartIteration(startIteration);
        aTerm.setThetaInterval(thetaInterval);
        aTerm.setThetaThreshold(thetaThreshold);
        aTerm.setThetaFactor(thetaFactor);
        context.addEngineListener(aTerm);

        /** Do some debugging stuff */
        if (logInterval > 0) context.addEngineListener(new HamiltonianLogger(
                calibrator,
                logInterval,
                name,
                context.getIoContext().getRoot(),
                startIteration));

        if (debugInterval > 0 && context.getIoContext() != null) {
            context.addEngineListener(new ODDistributionDebugger(
                    calibrator,
                    name + ".internState",
                    context.getIoContext().getPath(),
                    debugInterval));
            aTerm.enableFileLogging(context.getIoContext().getPath() + "/" + name + ".thetaUpdates.txt");
            calibrator.setDebugFile(context.getIoContext().getPath() + "/" + name + ".debug.txt");
        }

        /** Add hamiltonian analyzer */
        if (attachAnalyzers) {
            AnalyzerTaskComposite<Collection<? extends Person>> analyzers = new AnalyzerTaskComposite<>();
            AnalyzerTaskGroup<Collection<? extends Person>> group = new AnalyzerTaskGroup<>(analyzers, context.getIoContext(), name);
            context.addAnalyzer(group);

            AnalyzerTaskComposite<Pair<NumericMatrix, NumericMatrix>> composite = new AnalyzerTaskComposite<>();

            HistogramWriter writer = new HistogramWriter(context.getIoContext(), new PassThroughDiscretizerBuilder(new
                    LinearDiscretizer(0.05), "linear"));

            MatrixVolumeCompare volTask = new MatrixVolumeCompare(String.format("matrix.%s.vol", name));
            volTask.setIoContext(context.getIoContext());
            volTask.setHistogramWriter(writer);

            MatrixDistanceCompare distTask = new MatrixDistanceCompare(String.format("matrix.%s.dist", name), zones);
            distTask.setFileIoContext(context.getIoContext());

            MatrixMarginalsCompare marTask = new MatrixMarginalsCompare(String.format("matrix.%s", name));
            marTask.setHistogramWriter(writer);

            composite.addComponent(volTask);
            composite.addComponent(distTask);
            composite.addComponent(marTask);

            DefaultMatrixBuilderFactory factory = new DefaultMatrixBuilderFactory();
            MatrixComparator analyzer = new MatrixComparator(refMatrix, factory.create(placeIndex, zones), composite);
            analyzer.setLegPredicate(predicate);
            analyzer.setVolumeThreshold(volumeThreshold);
            analyzer.setMatrixPredicate(new ZoneDistancePredicate(zones, minDistanceThreshold, maxDistanceThreshold, CartesianDistanceCalculator.getInstance()));
            analyzer.setUseWeights(useWeights);
            analyzer.setNormalize(normalize);
            analyzers.addComponent(analyzer);
        }

        return aTerm;
    }
}
