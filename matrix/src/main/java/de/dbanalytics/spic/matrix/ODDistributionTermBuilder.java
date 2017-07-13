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

import de.dbanalytics.spic.analysis.AnalyzerTaskComposite;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.HistogramWriter;
import de.dbanalytics.spic.analysis.PassThroughDiscretizerBuilder;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.gis.ActivityLocationLayer;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.sim.AnnealingHamiltonian;
import de.dbanalytics.spic.sim.AttributeChangeListenerComposite;
import de.dbanalytics.spic.sim.HamiltonianLogger;
import de.dbanalytics.spic.sim.MarkovEngineListenerComposite;
import org.apache.commons.lang3.tuple.Pair;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.facilities.ActivityFacilities;

import java.util.Collection;

/**
 * @author johannes
 */
public class ODDistributionTermBuilder {

    private ActivityFacilities facilities;

    private ZoneCollection zones;

    private NumericMatrix refMatrix;

    private double distanceThreshold = 0;

    private double volumeThreshold = 0;

    private boolean useWeights = false;

    private double thetaMin = 1.0;

    private double thetaMax = 1.0;

    private double thetaFactor = 10;

    private long thetaInterval = (long) 1e7;

    private double thetaThreshold = 0.005;

    private long startIteration = 0;

    private long logInterval = 0;

    private String name;

    private AttributeChangeListenerComposite attributeListeners;

    private MarkovEngineListenerComposite engineListeners;

    private FileIOContext ioContext;

    private AnalyzerTaskComposite<Collection<? extends Person>> analyzers;

    public ODDistributionTermBuilder(NumericMatrix refMatrix, ActivityFacilities facilities, ZoneCollection zones) {
        this.refMatrix = refMatrix;
        this.facilities = facilities;
        this.zones = zones;
    }

    public ODDistributionTermBuilder distanceThreshold(double distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
        return this;
    }

    public ODDistributionTermBuilder volumeThreshold(double volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
        return this;
    }

    public ODDistributionTermBuilder useWeights(boolean useWeights) {
        this.useWeights = useWeights;
        return this;
    }

    public ODDistributionTermBuilder thetaMin(double thetaMin) {
        this.thetaMin = thetaMin;
        return this;
    }

    public ODDistributionTermBuilder thetaMax(double thetaMax) {
        this.thetaMax = thetaMax;
        return this;
    }

    public ODDistributionTermBuilder thetaFactor(double thetaFactor) {
        this.thetaFactor = thetaFactor;
        return this;
    }

    public ODDistributionTermBuilder thetaInterval(long thetaInterval) {
        this.thetaInterval = thetaInterval;
        return this;
    }

    public ODDistributionTermBuilder thetaThreshold(double thetaThreshold) {
        this.thetaThreshold = thetaThreshold;
        return this;
    }

    public ODDistributionTermBuilder startIteration(long startIteration) {
        this.startIteration = startIteration;
        return this;
    }

    public ODDistributionTermBuilder logInterval(long logInterval) {
        this.logInterval = logInterval;
        return this;
    }

    public ODDistributionTermBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ODDistributionTermBuilder attributeListeners(AttributeChangeListenerComposite attributeListeners) {
        this.attributeListeners = attributeListeners;
        return this;
    }

    public ODDistributionTermBuilder engineListeners(MarkovEngineListenerComposite engineListeners) {
        this.engineListeners = engineListeners;
        return this;
    }

    public ODDistributionTermBuilder ioContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
        return this;
    }

    public ODDistributionTermBuilder analyzers(AnalyzerTaskComposite<Collection<? extends Person>> analyzers) {
        this.analyzers = analyzers;
        return this;
    }

    public ODCalibrator build() {
        ODCalibrator calibrator = new ODCalibrator.Builder(refMatrix, zones, facilities).build();

        calibrator.setDistanceThreshold(distanceThreshold);
        calibrator.setVolumeThreshold(volumeThreshold);
        calibrator.setUseWeights(false);

        /** Add to facility attribute change listener **/
        if (attributeListeners != null) attributeListeners.addComponent(calibrator);

        /** Wrap in an annealing hamiltonian */
        AnnealingHamiltonian aTerm = new AnnealingHamiltonian(calibrator, thetaMin, thetaMax);
        aTerm.setStartIteration(startIteration);
        aTerm.setThetaInterval(thetaInterval);
        aTerm.setThetaThreshold(thetaThreshold);
        aTerm.setThetaFactor(thetaFactor);
        if (engineListeners != null) engineListeners.addComponent(aTerm);

        /** Do some debugging stuff */
        if (logInterval > 0 && engineListeners != null) engineListeners.addComponent(new HamiltonianLogger(
                calibrator,
                logInterval,
                name,
                ioContext.getRoot(),
                startIteration));

        /** */
        if (analyzers != null) {
            AnalyzerTaskComposite<Pair<NumericMatrix, NumericMatrix>> composite = new AnalyzerTaskComposite<>();

            HistogramWriter writer = new HistogramWriter(ioContext, new PassThroughDiscretizerBuilder(new
                    LinearDiscretizer(0.05), "linear"));

            MatrixVolumeCompare volTask = new MatrixVolumeCompare(String.format("matrix.%s.vol", name));
            volTask.setIoContext(ioContext);
            volTask.setHistogramWriter(writer);

            MatrixDistanceCompare distTask = new MatrixDistanceCompare(String.format("matrix.%s.dist", name), zones);
            distTask.setFileIoContext(ioContext);

            MatrixMarginalsCompare marTask = new MatrixMarginalsCompare(String.format("matrix.%s", name));
            marTask.setHistogramWriter(writer);

            composite.addComponent(volTask);
            composite.addComponent(distTask);
            composite.addComponent(marTask);

            DefaultMatrixBuilderFactory factory = new DefaultMatrixBuilderFactory();
            ActivityLocationLayer locationLayer = new ActivityLocationLayer(facilities);
            MatrixComparator analyzer = new MatrixComparator(refMatrix, factory.create(locationLayer, zones), composite);
            analyzer.setVolumeThreshold(volumeThreshold);

            analyzers.addComponent(analyzer);
        }

        return calibrator;
    }
}
