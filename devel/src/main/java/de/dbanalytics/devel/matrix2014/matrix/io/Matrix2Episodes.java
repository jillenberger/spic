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

package de.dbanalytics.devel.matrix2014.matrix.io;

import de.dbanalytics.devel.matrix2014.gis.FacilityUtils;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.gis.*;
import de.dbanalytics.spic.matrix.MatrixOperations;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;
import de.dbanalytics.spic.util.Executor;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.common.util.ProgressLogger;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesReaderMatsimV1;

import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class Matrix2Episodes {

    private static final String MODULE_NAME = "matrix2population";

    private static final String MATRIX_FILE_PARAM = "matrixFile";

    private static final String ZONES_FILE_PARAM = "zonesFile";

//    private static final String ZONE_KEY_PARAM = "zoneKey";

    private static final String FACILITY_FILE_PARAM = "facilityFile";

    private static final String THRESHOLD_PARAM = "threshold";

    private static final String SAMPLE_SIZE_PARAM = "sampleSize";

    private static final String PERSONS_FILE_PARAM = "personsFile";

    public static void main(String args[]) throws IOException {
        final Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);
        ConfigGroup group = config.getModule(MODULE_NAME);
        /*
        Load matrix.
         */
        logger.info("Loading matrix...");
        NumericMatrix m = NumericMatrixIO.read(group.getValue(MATRIX_FILE_PARAM));
        /*
        Load zones.
         */
        logger.info("Loading zones...");
        Set<Feature> features = new FeaturesIO().read(group.getValue(ZONES_FILE_PARAM));
        ZoneIndex zoneCollection = new ZoneIndex(features);

        /*
        Load facilities.
         */
        logger.info("Loading facilities...");
        Scenario scenario = ScenarioUtils.createScenario(config);
        FacilitiesReaderMatsimV1 reader = new FacilitiesReaderMatsimV1(scenario);
        reader.readFile(group.getValue(FACILITY_FILE_PARAM));
        /*
        Initialize.
         */
        logger.info("Initializing...");
        Matrix2Episodes m2e = new Matrix2Episodes(zoneCollection, scenario.getActivityFacilities());
        /*
        Set params.
         */
        if(group.getValue(THRESHOLD_PARAM) != null) {
            m2e.setThreshold(Double.parseDouble(group.getValue(THRESHOLD_PARAM)));
        }

        if(group.getValue(SAMPLE_SIZE_PARAM) != null) {
            int n_persons = Integer.parseInt(group.getValue(SAMPLE_SIZE_PARAM));
            double n_trips = MatrixOperations.sum(m);
            double factor = n_persons/n_trips;
            m2e.setFactor(factor);
        }
        /*
        Generating persons.
         */
        logger.info("Generating persons...");
        Set<Person> persons = m2e.generate(m);
        /*
        Write out.
         */
        logger.info("Writing persons...");
        PopulationIO.writeToXML(group.getValue(PERSONS_FILE_PARAM), persons);
        logger.info("Done.");
        Executor.shutdown();
    }

    private static final Logger logger = Logger.getLogger(Matrix2Episodes.class);

    private ZoneIndex zoneCollection;

    private Map<Feature, List<ActivityFacility>> zoneFacilities;

    private Random random;

    private double factor;

    private double threshold;

    public  Matrix2Episodes(ZoneIndex zoneCollection, ActivityFacilities facilities) {
        setRandom(new XORShiftRandom());
        setFactor(1.0);
        setThreshold(0);

        this.zoneCollection = zoneCollection;
        zoneFacilities = FacilityUtils.mapFacilities2Zones(zoneCollection, facilities);
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    private Set<Person> generate(NumericMatrix m) {
        Set<String> keys = m.keys();

        Set<String> zonesNotFound = new HashSet<>();
        Set<String> zonesNoFacilities = new HashSet<>();

        int lowVolumeCnt = 0;
        double lowVolumeSum = 0;

        int idCounter = 0;

        Set<Person> persons = new HashSet<>();

        ProgressLogger.init(keys.size(), 2, 10);

        for(String i : keys) {
            Feature z_i = zoneCollection.get(i);
            List<ActivityFacility> facs_i = zoneFacilities.get(z_i);

            if(facs_i == null || facs_i.isEmpty()) {
                zonesNoFacilities.add(i);

            } else {
                if (z_i != null) {
                    for (String j : keys) {
                        Feature z_j = zoneCollection.get(j);
                        List<ActivityFacility> facs_j = zoneFacilities.get(z_j);

                        if(facs_j == null || facs_j.isEmpty()) {
                            zonesNoFacilities.add(j);

                        } else {
                            if (z_j != null) {
                                Double volume = m.get(i, j);
                                if (volume != null) {

                                    if (volume > threshold) {
                                        /*
                                        Always round up...
                                        */
                                        double n = Math.ceil(volume * factor);
                                        /*
                                        ... but adjust the weight correspondingly.
                                        */
                                        double w = volume / n;

                                        for (int k = 0; k < n; k++) {
                                            String originFac = facs_i.get(random.nextInt(facs_i.size())).getId().toString();
                                            String destFac = facs_j.get(random.nextInt(facs_j.size())).getId().toString();

                                            Person p = buildPerson(String.valueOf(idCounter++), originFac, destFac);
                                            p.setAttribute(CommonKeys.WEIGHT, String.valueOf(w));

                                            persons.add(p);
                                        }
                                    } else {
                                        lowVolumeCnt++;
                                        lowVolumeSum += volume;
                                    }
                                }
                            } else {
                                zonesNotFound.add(j);
                            }
                        }
                    }
                } else {
                    zonesNotFound.add(i);
                }
            }
            ProgressLogger.step();
        }
        ProgressLogger.terminate();

        if(!zonesNotFound.isEmpty()) logger.warn(String.format("%s zones not found.", zonesNotFound.size()));
        if(!zonesNoFacilities.isEmpty()) logger.warn(String.format("%s zone with no facilities.", zonesNoFacilities.size()));
        if(lowVolumeCnt > 0)
            logger.warn(String.format("%s relations with volume below threshold. Lost %s trips.", lowVolumeCnt, lowVolumeSum));

        return persons;
    }

    private Person buildPerson(String id, String origin, String destination) {
        Person p = new PlainPerson(id);
        Episode e = new PlainEpisode();
        p.addEpisode(e);

        Segment originAct = new PlainSegment();
        originAct.setAttribute(CommonKeys.PLACE, origin);
        e.addActivity(originAct);

        Segment trip = new PlainSegment();
        e.addLeg(trip);

        Segment destinationAct = new PlainSegment();
        destinationAct.setAttribute(CommonKeys.PLACE, destination);
        e.addActivity(destinationAct);

        return p;
    }
}
