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

import org.apache.log4j.Logger;
import org.geotools.referencing.CRS;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.contrib.common.gis.CRSUtils;
import org.matsim.contrib.common.util.XORShiftRandom;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.network.NetworkWriter;
import org.matsim.core.network.NodeImpl;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.population.PopulationReader;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.ActivityFacility;
import org.matsim.facilities.ActivityFacilityImpl;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.facilities.MatsimFacilitiesReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.*;

/**
 * @author johannes
 *
 */
public class DemoScenario {

	/**
	 * @param args
	 * @throws FactoryException 
	 */
	public static void main(String[] args) throws FactoryException {
		String popFile = args[0];
		String facFile = args[1];
		String netFile = args[2];
		int n = Integer.parseInt(args[3]);
		String outDir = args[4];
		
		Logger logger = Logger.getLogger(DemoScenario.class);
		
		MathTransform transform = CRS.findMathTransform(CRSUtils.getCRS(31467), CRSUtils.getCRS(3857));
		
		Config config = ConfigUtils.createConfig();
		Scenario scenario = ScenarioUtils.createScenario(config);
		/*
		 * extract subsample
		 */
		logger.info("Loading persons...");
		PopulationReader pReader = new MatsimPopulationReader(scenario);
		pReader.readFile(popFile);
		logger.info("Done.");
		
		logger.info("Drawing population subsample...");
		List<Person> persons = new ArrayList<>(scenario.getPopulation().getPersons().values());
		Collections.shuffle(persons);
		Population population = PopulationUtils.createPopulation(config);
		for(int i = 0; i < n; i++) {
			population.addPerson(persons.get(i));
		}
		logger.info("Done.");
		
		logger.info("Blurring activity end times...");
		Random random = new XORShiftRandom();
		int range = 300;
		for(Person person : population.getPersons().values()) {
			for(Plan plan : person.getPlans()) {
				for(int i = 0; i < plan.getPlanElements().size(); i+=2) {
					Activity act = (Activity) plan.getPlanElements().get(i);
					double endTim = act.getEndTime() - range + (random.nextDouble() * range * 2);
					act.setEndTime(endTim);
					double startTim = act.getStartTime() - range + (random.nextDouble() * range * 2);
					act.setStartTime(startTim);

				}
			}
		}
		logger.info("Done.");

		logger.info("Transforming activity coordinates...");
		for(Person person : population.getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				for(int i = 0; i < plan.getPlanElements().size(); i+=2) {
					ActivityImpl act = (ActivityImpl) plan.getPlanElements().get(i);
					double[] points = new double[] { act.getCoord().getX(), act.getCoord().getY() };
					try {
						transform.transform(points, 0, points, 0, 1);
					} catch (TransformException e) {
						e.printStackTrace();
					}
					act.setCoord(new Coord(points[0], points[1]));
				}
			}
		}
		logger.info("Done.");

		logger.info("Writing population...");
		PopulationWriter pWriter = new PopulationWriter(population, scenario.getNetwork());
		pWriter.write(String.format("%s/plans.xml.gz", outDir));
		logger.info("Done.");
		/*
		 * filter only used facilities
		 */
		logger.info("Loading facilities...");
		MatsimFacilitiesReader fReader = new MatsimFacilitiesReader(scenario);
		fReader.readFile(facFile);
		logger.info("Done.");
		
		logger.info("Removing unused facilities...");
		Set<Id<ActivityFacility>> unused = new HashSet<>(scenario.getActivityFacilities().getFacilities().keySet());
		for(Person person : population.getPersons().values()) {
			for(Plan plan : person.getPlans()) {
				for(int i = 0; i < plan.getPlanElements().size(); i+=2) {
					Activity act = (Activity) plan.getPlanElements().get(i);
					unused.remove(act.getFacilityId());
				}
			}
		}
		logger.info("Done.");
		
		logger.info("Transforming facility coordinates...");
		for(ActivityFacility fac : scenario.getActivityFacilities().getFacilities().values()) {
			double[] points = new double[] { fac.getCoord().getX(), fac.getCoord().getY() };
			try {
				transform.transform(points, 0, points, 0, 1);
			} catch (TransformException e) {
				e.printStackTrace();
			}

			((ActivityFacilityImpl)fac).setCoord(new Coord(points[0], points[1]));
		}
		logger.info("Done.");
		
		logger.info("Writing facilities...");
		FacilitiesWriter fWriter = new FacilitiesWriter(scenario.getActivityFacilities());
		fWriter.write(String.format("%s/facilities.xml.gz", outDir));
		logger.info("Done.");
		/*
		 * clean network from foreign links
		 */
		logger.info("Loading network...");
		MatsimNetworkReader nReader = new MatsimNetworkReader(scenario.getNetwork());
		nReader.readFile(netFile);
		logger.info("Done.");

		logger.info("Transforming node coordinates...");
		for(Node node : scenario.getNetwork().getNodes().values()) {
			double[] points = new double[] { node.getCoord().getX(), node.getCoord().getY() };
			try {
				transform.transform(points, 0, points, 0, 1);
			} catch (TransformException e) {
				e.printStackTrace();
			}

			((NodeImpl)node).setCoord(new Coord(points[0], points[1]));
		}
		logger.info("Done.");
		
		logger.info("Writing network...");
		NetworkWriter nWriter = new NetworkWriter(scenario.getNetwork());
		nWriter.write(String.format("%s/network.xml.gz", outDir));
		logger.info("Done.");
	}

}
