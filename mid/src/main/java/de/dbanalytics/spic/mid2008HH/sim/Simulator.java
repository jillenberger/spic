/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 * Project de.dbanalytics.spic.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.dbanalytics.spic.mid2008HH.sim;

import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.gis.DataPool;
import de.dbanalytics.spic.sim.*;
import de.dbanalytics.spic.util.Executor;
import gnu.trove.map.TDoubleDoubleMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author jillenberger
 */
public class Simulator {

    static final String MODULE_NAME = "synPopSim";
    private static final Logger logger = Logger.getLogger(Simulator.class);
    private AnalyzerTaskComposite<Collection<? extends Person>> analyzerTasks;

    private AnalyzerTaskComposite<Collection<? extends Person>> hamiltonianAnalyzers;

    private HamiltonianComposite hamiltonian;

    private MarkovEngineListenerComposite engineListeners;

    private Set<? extends Person> refPersons;

    private Set<? extends Person> simPersons;

    private Map<String, AttributeChangeListenerComposite> attributeListeners;

    private long loggingInterval;

    private FileIOContext ioContext;

    private DataPool dataPool;

    private Random random;

    public static void main(String args[]) throws IOException {
        Logger.getRootLogger().setLevel(Level.TRACE);

        final Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);

        long iterations = (long) Double.parseDouble(config.getParam(Simulator.MODULE_NAME, "iterations"));

        MarkovEngine engine = new Simulator().build(config);
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

    MarkovEngineListenerComposite getEngineListeners() {
        return engineListeners;
    }

    Set<? extends Person> getRefPersons() {
        return refPersons;
    }

    Set<? extends Person> getSimPersons() {
        return simPersons;
    }

    Map<String, AttributeChangeListenerComposite> getAttributeListeners() {
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

    public MarkovEngine build(Config config) throws IOException {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);
        /*
        Initialize composites...
         */
        hamiltonian = new HamiltonianComposite();
        analyzerTasks = new AnalyzerTaskComposite<>();
        engineListeners = new MarkovEngineListenerComposite();
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
        refPersons = RefPopulationBuilderDrive.build(this, config);
        /*
        Generate the simulation population...
         */
        simPersons = SimPopulationBuilderDrive.build(this, config);
        /*
        Setup listeners for changes on facilities and geo distance.
		 */
        attributeListeners.put(CommonKeys.LEG_GEO_DISTANCE, new AttributeChangeListenerComposite());
        attributeListeners.put(CommonKeys.ACTIVITY_FACILITY, new AttributeChangeListenerComposite());

        GeoDistanceUpdater geoDistanceUpdater = new GeoDistanceUpdater(attributeListeners.get(CommonKeys.LEG_GEO_DISTANCE));

        attributeListeners.get(CommonKeys.ACTIVITY_FACILITY).addComponent(geoDistanceUpdater);
        /*
        Build default analyzer...
         */
        DefaultAnalyzerBuilder.build(this, config);
		/*
        Build hamiltonians...
         */

        hamiltonianAnalyzers = new ConcurrentAnalyzerTask<>();
        analyzerTasks.addComponent(new AnalyzerTaskGroup<>(hamiltonianAnalyzers, ioContext, "hamiltonian"));

        GeoDistanceZoneHamiltonianDrive.build(this, config);
//        TargetDistanceBuilder.build(this, config);
//        ModeGeoDistanceBuilder.build(this, config);
        setupModeDistance(config);
        Lau2GeoDistanceBuilder.build(this, config);

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

        engineListeners.addComponent(new AnalyzerListener(analyzerTasks, ioContext, dumpInterval));
        /*
		Setup the facility mutator...
		 */
        FacilityMutatorBuilder mutatorBuilder = new FacilityMutatorBuilder(dataPool, random);
        mutatorBuilder.addToBlacklist(ActivityTypes.HOME);
        mutatorBuilder.setListener(attributeListeners.get(CommonKeys.ACTIVITY_FACILITY));
        mutatorBuilder.setProximityProbability(Double.parseDouble(configGroup.getValue("proximityProba")));
        Mutator<? extends Attributable> mutator = mutatorBuilder.build();
        /*
        Create the markov engine...
         */
        MarkovEngine engine = new MarkovEngine(simPersons, hamiltonian, mutator, random);
        engine.setListener(engineListeners);

        return engine;
    }

    private void setupModeDistance(Config config) throws IOException {
        ConfigGroup group = config.getModule("ModeGeoDistance");
        String baseDir = group.getValue("base_dir");

        Set<String> modes = new HashSet<>();
        for (Person person : getSimPersons()) {
            for (Episode episode : person.getEpisodes()) {
                for (Segment leg : episode.getLegs()) {
                    String value = leg.getAttribute(CommonKeys.LEG_MODE);
                    if (value != null) modes.add(value);
                }
            }
        }

        for (String mode : modes) {
            String histFile = String.format("%s/%s.%s.txt", baseDir, CommonKeys.LEG_GEO_DISTANCE, mode);
            TDoubleDoubleMap hist = Histogram.readHistogram(histFile);
            LegDistributionTermBuilder builder = new LegDistributionTermBuilder(hist, CommonKeys.LEG_GEO_DISTANCE);
            new LegDistributionTermConfig(group).configure(builder);

            Predicate<Segment> predicate = new LegAttributePredicate(CommonKeys.LEG_MODE, mode);
            builder.analyzers(getHamiltonianAnalyzers());
            builder.attributeListeners(getAttributeListeners().get(CommonKeys.LEG_GEO_DISTANCE));
            builder.engineListeners(getEngineListeners());
            builder.ioContext(getIOContext());
            builder.predicate(predicate);
            builder.name(CommonKeys.LEG_GEO_DISTANCE + "." + mode);
            Hamiltonian h = builder.build();

            getHamiltonian().addComponent(h);
        }


    }
}
