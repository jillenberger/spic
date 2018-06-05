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

import com.vividsolutions.jts.geom.Coordinate;
import de.dbanalytics.devel.matrix2014.data.DataPool;
import de.dbanalytics.devel.matrix2014.gis.*;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.mid2008.MiDKeys;
import de.dbanalytics.spic.processing.CopyPersonAttToLeg;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.sim.AnnealingHamiltonian;
import de.dbanalytics.devel.matrix2014.sim.BivariatMean;
import de.dbanalytics.spic.sim.HamiltonianLogger;
import de.dbanalytics.spic.sim.McmcSimulationObserver;
import de.dbanalytics.spic.sim.config.AnnealingHamiltonianConfigurator;
import de.dbanalytics.spic.sim.data.CachedPerson;
import de.dbanalytics.spic.sim.data.Converters;
import de.dbanalytics.spic.sim.data.DoubleConverter;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.matsim.api.core.v01.Id;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.Facility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class MeanZoneDistanceHamiltonian {

    public static final String MODULE_NAME = "meanDistanceHamiltonian";

    public static final String PERSON_ZONE_IDX = "zoneIndex";

    public static void build(Simulator engine, Config config) {
        ConfigGroup configGroup = config.getModule(MODULE_NAME);

        DataPool dataPool = engine.getDataPool();
        ActivityFacilities facilities = ((FacilityData) dataPool.get(FacilityDataLoader.KEY)).getAll();
//        ZoneCollection zones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("lau2");
        ZoneCollection zones = ((ZoneData) dataPool.get(ZoneDataLoader.KEY)).getLayer("nuts3");
        TObjectIntMap<Zone> indices = indexZones(engine.getSimPersons(), facilities, zones);
//        TIntDoubleMap meanDistances = lau2MeanDistance(engine.getRefPersons(), engine);
//        TIntDoubleMap refValues = generateRefValues(indices, meanDistances, zones);
        TIntDoubleMap refValues = generateRefValues(engine.getRefPersons(), engine, indices);
//        /*
//        Copy the lau2 class attribute from the person element to the corresponding leg elements.
//         */
//        TaskRunner.run(new CopyPersonAttToLeg(MiDKeys.PERSON_LAU2_CLASS), engine.getRefPersons());
        TaskRunner.run(new CopyPersonAttToLeg(PERSON_ZONE_IDX), engine.getSimPersons());
        /*
        Get the legs.
         */
//        Set<Attributable> refLegs = getCarLegs(engine.getRefPersons(), engine.getLegPredicate());
        Set<Attributable> simLegs = getCarLegs(engine.getSimPersons(), engine.getLegPredicate());
        /*
        Build and add the hamiltonian.
         */
        Converters.register(MiDKeys.PERSON_LAU2_CLASS, DoubleConverter.getInstance());
        Converters.register(PERSON_ZONE_IDX, DoubleConverter.getInstance());

        BivariatMean hamiltonian = new BivariatMean(
                refValues,
                simLegs,
                PERSON_ZONE_IDX,
                Attributes.KEY.BEELINE_DISTANCE,
                new LinearDiscretizer(1.0),
                engine.getUseWeights());

        AnnealingHamiltonian annealingHamiltonian = AnnealingHamiltonianConfigurator.configure(
                hamiltonian,
                configGroup);
        engine.getHamiltonian().addComponent(annealingHamiltonian);
        engine.getEngineListeners().addComponent(annealingHamiltonian);
        engine.getAttributeListeners().get(Attributes.KEY.BEELINE_DISTANCE).addComponent(hamiltonian);

        String errOut = engine.getIOContext().getRoot() + "/meanZoneDistance";
        new File(errOut).mkdirs();
        engine.getEngineListeners().addComponent(new ErrorStatsWriter(errOut, hamiltonian));
        /*
        Add a hamiltonian logger.
         */
        engine.getEngineListeners().addComponent(new HamiltonianLogger(hamiltonian,
                engine.getLoggingInterval(),
                "meanZoneDistance",
                engine.getIOContext().getRoot()));

    }

    private static TObjectIntMap indexZones(Set<? extends Person> simPersons, ActivityFacilities facilities,
                                            ZoneCollection zones) {
        TObjectIntMap indices = new TObjectIntHashMap();
        int maxIdx = 0;
        for (Person person : simPersons) {
            Facility f = null;
            for (Episode episode : person.getEpisodes()) {
                for (Segment act : episode.getActivities()) {
                    if (ActivityTypes.HOME.equalsIgnoreCase(act.getAttribute(Attributes.KEY.TYPE))) {
                        String facilityId = act.getAttribute(Attributes.KEY.PLACE);
                        f = facilities.getFacilities().get(Id.create(facilityId, ActivityFacility.class));
                        break;
                    }
                }
            }

            if(f == null) {
                person.setAttribute(PERSON_ZONE_IDX, String.valueOf(0.0));
            } else {
                Zone z = zones.get(new Coordinate(f.getCoord().getX(), f.getCoord().getY()));
                if (z != null) {
                    int idx = indices.get(z);
                    if (idx == 0) {
                        maxIdx++;
                        indices.put(z, maxIdx);
                        idx = maxIdx;
                    }

                    person.setAttribute(PERSON_ZONE_IDX, String.valueOf((double) idx));
                } else {
                    person.setAttribute(PERSON_ZONE_IDX, String.valueOf(0.0));
                }
            }
        }

        return indices;
    }

    private static TIntDoubleMap lau2MeanDistance(Set<? extends Person> refPersons, Simulator engine) {
        TIntDoubleMap meanDistances = new TIntDoubleHashMap();

        for (int klass = 0; klass < 6; klass++) {
            Predicate<Segment> lauPred = new LegPersonAttributePredicate(MiDKeys.PERSON_LAU2_CLASS, String.valueOf(klass));
            Predicate<Segment> predicate = PredicateAndComposite.create(engine.getLegPredicate(), lauPred);

            NumericAnalyzer analyzer = NumericLegAnalyzer.create(
                    Attributes.KEY.BEELINE_DISTANCE,
                    engine.getUseWeights(),
                    predicate,
                    null,
                    null);

            List<StatsContainer> containers = new ArrayList<>();
            analyzer.analyze(refPersons, containers);
            meanDistances.put(klass, containers.get(0).getMean());
        }

        return meanDistances;
    }

    private static TIntDoubleMap generateRefValues(Collection<? extends Person> refPersons, Simulator engine, TObjectIntMap<Zone> indices) {
        TIntDoubleMap values = new TIntDoubleHashMap();
        NumericAnalyzer analyzer = NumericLegAnalyzer.create(
                Attributes.KEY.BEELINE_DISTANCE,
                engine.getUseWeights(),
                engine.getLegPredicate(),
                null,
                null);

        List<StatsContainer> containers = new ArrayList<>();
        analyzer.analyze(refPersons, containers);
        double mean = containers.get(0).getMean();

        int[] keys = indices.values();
        for(int index : keys) {
            values.put(index, mean);
        }
        return values;

//        double dummyMean = StatUtils.mean(meanDistances.values());
//        TIntDoubleMap values = new TIntDoubleHashMap();
//        TObjectIntIterator<Zone> it = indices.iterator();
//        for (int i = 0; i < indices.size(); i++) {
//            it.advance();
//            int idx = it.value();
//            if(idx == 0) {
//                values.put(idx, dummyMean);
//            } else {
//                Zone zone = it.key();
//                int klass = Integer.parseInt(zone.getAttribute(MiDKeys.PERSON_LAU2_CLASS));
//                double mean = meanDistances.get(klass);
//
//                values.put(idx, mean);
//            }
//        }
//
//        return values;
    }

    private static Set<Attributable> getCarLegs(Set<? extends Person> persons, Predicate<Segment> predicate) {
        Set<Attributable> legs = new HashSet<>();
        for (Person p : persons) {
            Episode e = p.getEpisodes().get(0);
            for (Segment leg : e.getLegs()) {
                if (predicate.test(leg)) legs.add(leg);
            }
        }

        return legs;
    }

    private static class ErrorStatsWriter implements McmcSimulationObserver {

        private final String outputDir;
        private final BivariatMean hamiltonian;
        private long interval = (long) 1e8;
        private long iteration;

        public ErrorStatsWriter(String outputDir, BivariatMean hamiltonian) {
            this.outputDir = outputDir;
            this.hamiltonian = hamiltonian;
        }

        @Override
        public void afterStep(Collection<CachedPerson> population, Collection<? extends Attributable> mutations, boolean accepted) {
            if (iteration % interval == 0) {
                try {
                    double[] errors = hamiltonian.getBinErrors();
                    Arrays.sort(errors);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s/%s.txt", outputDir,
                            iteration)));
                    for (double err : errors) {
                        writer.write(String.valueOf(err));
                        writer.newLine();
                    }

                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            iteration++;
        }
    }
}
