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

package de.dbanalytics.devel.drive;

import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.devel.matrix2014.gis.Zone;
import de.dbanalytics.devel.matrix2014.gis.ZoneCollection;
import de.dbanalytics.devel.matrix2014.gis.ZoneGeoJsonIO;
import de.dbanalytics.spic.util.MatsimCoordUtils;
import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.joda.time.LocalTime;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.common.gis.CRSUtils;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacilities;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.FacilitiesReaderMatsimV1;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by johannesillenberger on 11.04.17.
 */
public class RequestGenerator {

    private static final Logger logger = Logger.getLogger(RequestGenerator.class);

    private static final String MODULE_NAME = "requestGenerator";

    private static final String FACILITY_FILE_PARAM = "facilityFile";

    private static final String PERSONS_FILE_PARAM = "personsFile";

    private static final String PROBA_PARAM = "requestProba";

    private static final String REQUESTS_FILE_PARAM = "requestsFile";

    private static final String ZONES_FILE_PARAM = "zonesFile";

    private static final String SEPARATOR = ";";

    public static void main(String args[]) throws IOException, FactoryException {
        final Config config = new Config();
        ConfigUtils.loadConfig(config, args[0]);
        ConfigGroup group = config.getModules().get(MODULE_NAME);

        logger.info("Loading facilities...");
        Scenario scenario = ScenarioUtils.createScenario(config);
        FacilitiesReaderMatsimV1 reader = new FacilitiesReaderMatsimV1(scenario);
        reader.readFile(group.getValue(FACILITY_FILE_PARAM));

        logger.info("Loading persons...");
        Set<? extends Person> persons = PopulationIO.loadFromXML(group.getParams().get(PERSONS_FILE_PARAM), new PlainFactory());

        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(
                group.getParams().get(ZONES_FILE_PARAM),
                "NO",
                null);
//        Zone source = zones.get("1056044");
//        Zone target = zones.get("2000005");

        logger.info("Selecting trips...");
        double proba = Double.parseDouble(group.getParams().get(PROBA_PARAM));
        Random random = new XORShiftRandom();

        List<Segment> trips = new ArrayList<>(persons.size());
        int cnt = 0;
        for (Person person : persons) {
            Episode episode = person.getEpisodes().get(0);
            for (Segment leg : episode.getLegs()) {
                cnt++;
                if (checkInZones(leg, scenario.getActivityFacilities(), zones)) {
                    if (random.nextDouble() < proba) {
                        trips.add(leg);
                    }
                }
            }
        }
        logger.info(String.format("Parsed %s trips.", cnt));
        logger.info(String.format("Selected %s trips.", trips.size()));

        logger.info("Sorting trips...");
        if (trips.size() > 1) {
            Collections.sort(trips, new Comparator<Segment>() {
                @Override
                public int compare(Segment segment, Segment t1) {
                    double time1 = Double.parseDouble(segment.getAttribute(CommonKeys.DEPARTURE_TIME));
                    double time2 = Double.parseDouble(t1.getAttribute(CommonKeys.DEPARTURE_TIME));
                    int r = Double.compare(time1, time2);
                    if (r == 0) {
                        if (segment.equals(t1)) return 0;
                        else return segment.hashCode() - t1.hashCode();
                    }
                    return r;
                }
            });
        }

        logger.info("Writing trips...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(group.getParams().get(REQUESTS_FILE_PARAM)));
        writer.write("Id;Time;From_lat;From_lon;To_lat;To_lon");
        writer.newLine();

        MathTransform transform = CRS.findMathTransform(CRSUtils.getCRS(31467), DefaultGeographicCRS.WGS84);

        Map<Id<ActivityFacility>, ? extends ActivityFacility> facilities = scenario.getActivityFacilities().getFacilities();
        for (Segment leg : trips) {
            writer.write(leg.getEpisode().getPerson().getId().toString());
            writer.write(SEPARATOR);
            int startTime = (int) Double.parseDouble(leg.getAttribute(CommonKeys.DEPARTURE_TIME));
            /** TODO: temporary fix: randomize start time*/
            double offset = (random.nextDouble() - 0.5) * 2 * 300;
            startTime += offset;
            /**-----*/
            LocalTime lt = LocalTime.MIDNIGHT.plusSeconds(startTime);
            writer.write(lt.toString("HH:mm:ss"));
            writer.write(SEPARATOR);

            String idStart = leg.previous().getAttribute(CommonKeys.PLACE);
            String idEnd = leg.next().getAttribute(CommonKeys.PLACE);

            ActivityFacility facStart = facilities.get(Id.create(idStart, ActivityFacility.class));
            ActivityFacility facEnd = facilities.get(Id.create(idEnd, ActivityFacility.class));

            double[] startCoord = new double[]{facStart.getCoord().getX(), facStart.getCoord().getY()};
            try {
                transform.transform(startCoord, 0, startCoord, 0, 1);
            } catch (TransformException e) {
                e.printStackTrace();
            }

            double[] endCoord = new double[]{facEnd.getCoord().getX(), facEnd.getCoord().getY()};
            try {
                transform.transform(endCoord, 0, endCoord, 0, 1);
            } catch (TransformException e) {
                e.printStackTrace();
            }
            writer.write(String.valueOf(startCoord[1]));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(startCoord[0]));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(endCoord[1]));
            writer.write(SEPARATOR);
            writer.write(String.valueOf(endCoord[0]));
            writer.newLine();
        }

        writer.close();
        logger.info("Done.");
    }

    static private boolean checkInZones(Segment leg, ActivityFacilities facilities, ZoneCollection zones) {
        Segment prevAct = leg.previous();
        Segment nextAct = leg.next();

        Id<ActivityFacility> prevId = Id.create(prevAct.getAttribute(CommonKeys.PLACE), ActivityFacility.class);
        Id<ActivityFacility> nextId = Id.create(nextAct.getAttribute(CommonKeys.PLACE), ActivityFacility.class);

        ActivityFacility prevFac = facilities.getFacilities().get(prevId);
        ActivityFacility nextFac = facilities.getFacilities().get(nextId);

        Point prevPoint = MatsimCoordUtils.coordToPoint(prevFac.getCoord());
        Point nextPoint = MatsimCoordUtils.coordToPoint(nextFac.getCoord());

        Zone source = zones.get(prevPoint.getCoordinate());
        Zone target = zones.get(nextPoint.getCoordinate());

        if (source != null && target != null) {
            if (source != target) return true;
            else return false;
        } else {
            return false;
        }
    }
}