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

import de.dbanalytics.devel.matrix2014.sim.FacilityMutatorBuilder;
import de.dbanalytics.devel.matrix2014.sim.GeoDistanceUpdaterFacility;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.devel.matrix2014.data.DataPool;
import de.dbanalytics.spic.processing.CopyPersonAttToLeg;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.sim.*;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;

import java.util.*;

/**
 * @author jillenberger
 */
public class Simulator {

    static final String MODULE_NAME = "synPopSim";
    private static final Logger logger = Logger.getLogger(Simulator.class);
    private static final boolean USE_WEIGHTS = true;

    private static final Predicate<Segment> DEFAULT_LEG_PREDICATE = new LegAttributePredicate(
            CommonKeys.MODE, CommonValues.LEG_MODE_CAR);
//    private static final Predicate<Segment> DEFAULT_LEG_PREDICATE = null;

    private static final String DEFAULT_PREDICATE_NAME = "car";
//    private static final String DEFAULT_PREDICATE_NAME = "";

    private AnalyzerTaskComposite<Collection<? extends Person>> analyzerTasks;

    private AnalyzerTaskComposite<Collection<? extends Person>> hamiltonianAnalyzers;

    private HamiltonianComposite hamiltonian;

    private McmcSimulationObserverComposite engineListeners;

    private Set<? extends Person> refPersons;

    private Set<? extends Person> simPersons;

    private Map<String, AttributeObserverComposite> attributeListeners;

    private long loggingInterval;

    private FileIOContext ioContext;

    private DataPool dataPool;

    private Random random;

    public static void main(String args[]) {
        Logger.getRootLogger().setLevel(Level.TRACE);

        final Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);

        long iterations = (long) Double.parseDouble(config.getParam(Simulator.MODULE_NAME, "iterations"));

        McmcSimulation engine = new Simulator().build(config);
        logger.info("Start sampling...");
        engine.run(iterations);
        logger.info("End sampling.");

        Executor.shutdown();
    }

    AnalyzerTaskComposite<Collection<? extends Person>> getAnalyzerTasks() {
        return analyzerTasks;
    }

    AnalyzerTaskComposite<Collection<? extends Person>> getHamiltonianAnalyzers() {
        return hamiltonianAnalyzers;
    }

    HamiltonianComposite getHamiltonian() {
        return hamiltonian;
    }

    McmcSimulationObserverComposite getEngineListeners() {
        return engineListeners;
    }

    boolean getUseWeights() {
        return USE_WEIGHTS;
    }

    Predicate<Segment> getLegPredicate() {
        return DEFAULT_LEG_PREDICATE;
    }

    String getLegPredicateName() {
        return DEFAULT_PREDICATE_NAME;
    }

    Set<? extends Person> getRefPersons() {
        return refPersons;
    }

    Set<? extends Person> getSimPersons() {
        return simPersons;
    }

    Map<String, AttributeObserverComposite> getAttributeListeners() {
        return attributeListeners;
    }

    long getLoggingInterval() {
        return loggingInterval;
    }

    FileIOContext getIOContext() {
        return ioContext;
    }

    DataPool getDataPool() {
        return dataPool;
    }

    Random getRandom() {
        return random;
    }

    public McmcSimulation build(Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /*
        Initialize composites...
         */
        hamiltonian = new HamiltonianComposite();
        analyzerTasks = new AnalyzerTaskComposite<>();
        engineListeners = new McmcSimulationObserverComposite();
        attributeListeners = new HashMap<>();
        /*
        Load parameters...
         */
        random = new XORShiftRandom(Long.parseLong(config.getParam("global", "randomSeed")));
        loggingInterval = (long) Double.parseDouble(configGroup.getValue("logInterval"));
        long dumpInterval = (long) Double.parseDouble(config.getParam(MODULE_NAME, "dumpInterval"));
        ioContext = new FileIOContext(configGroup.getValue("output"));
        /*
        Load GIS data...
         */
        dataPool = new DataPool();
        DataPoolLoader.load(this, config);
        /*
        Load reference population...
         */
        refPersons = RefPopulationBuilder.build(this, config);

        /*
        Generate the simulation population...
         */
        simPersons = SimPopulationBuilder.build(this, config);
        /*
		Setup listeners for changes on facilities and geo distance.
		 */
        attributeListeners.put(CommonKeys.BEELINE_DISTANCE, new AttributeObserverComposite());
        attributeListeners.put(CommonKeys.PLACE, new AttributeObserverComposite());

        GeoDistanceUpdaterFacility geoDistanceUpdater = new GeoDistanceUpdaterFacility(attributeListeners.get(CommonKeys.BEELINE_DISTANCE));
//        geoDistanceUpdater.setPredicate(new CachedModePredicate(CommonKeys.LEG_MODE, CommonValues.LEG_MODE_CAR));

        attributeListeners.get(CommonKeys.PLACE).addComponent(geoDistanceUpdater);
        /*
        Build default analyzer...
         */
        DefaultAnalyzerBuilder.build(this, config);
		/*
        Build hamiltonians...
         */
        if(getUseWeights()) {
            TaskRunner.run(new CopyPersonAttToLeg(CommonKeys.WEIGHT), refPersons);
            TaskRunner.run(new CopyPersonAttToLeg(CommonKeys.WEIGHT), simPersons);
        }

        hamiltonianAnalyzers = new ConcurrentAnalyzerTask<>();
        analyzerTasks.addComponent(new AnalyzerTaskGroup<>(hamiltonianAnalyzers, ioContext, "hamiltonian"));

//        GeoDistanceZoneDensityHamiltonian.getEdges(this, config);
        GeoDistanceZoneHamiltonian2.build(this, config);
//        GeoDistanceZoneHamiltonianDrive.getEdges(this, config);
//        PurposeHamiltonian.getEdges(this, config);
//        GeoDistanceTypeHamiltonian.getEdges(this, config);
//        GeoDistanceHamiltonian.getEdges(this, config);
//        GeoDistanceLAU2Hamiltonian.getEdges(this, config);
//        MeanDistanceHamiltonian.getEdges(this, config);
//        MeanZoneDistanceHamiltonian.getEdges(this, config);
//        ODCalibratorHamiltonian.getEdges(this, config);

        engineListeners.addComponent(new HamiltonianLogger(hamiltonian,
                loggingInterval,
                "SystemTemperature",
                ioContext.getRoot()));
        engineListeners.addComponent(new TransitionLogger(loggingInterval));
        /*
        Analyze reference population...
         */
        logger.info("Analyzing reference population...");
        ioContext.append("ref");
        getAnalyzerTasks().addComponent(new PopulationWriter(getIOContext()));
        AnalyzerTaskRunner.run(refPersons, analyzerTasks, ioContext);
        /*
        Extend the analyzer
         */
        ExtendedAnalyzerBuilder.build(this, config);
//        ExtendedAnalyzerBuilderDrive.getEdges(this, config);


        engineListeners.addComponent(new AnalyzerListener(analyzerTasks, ioContext, dumpInterval));
        /*
		Setup the facility mutator...
		 */
        FacilityMutatorBuilder mutatorBuilder = new FacilityMutatorBuilder(dataPool, random);
        mutatorBuilder.addToBlacklist(ActivityTypes.HOME);
        mutatorBuilder.setListener(attributeListeners.get(CommonKeys.PLACE));
        mutatorBuilder.setProximityProbability(Double.parseDouble(configGroup.getValue("proximityProba")));
        Mutator<? extends Attributable> mutator = mutatorBuilder.build();
        /*
        Create the markov engine...
         */
        McmcSimulation engine = new McmcSimulation(simPersons, hamiltonian, mutator, random);
        engine.setListener(engineListeners);

        return engine;
    }
}
