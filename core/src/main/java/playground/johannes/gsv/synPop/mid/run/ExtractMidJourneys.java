/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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

package playground.johannes.gsv.synPop.mid.run;

import org.apache.log4j.Logger;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.PlainPerson;
import de.dbanalytics.spic.data.io.XMLHandler;
import de.dbanalytics.spic.data.io.XMLWriter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author johannes
 *
 */
public class ExtractMidJourneys {

	private static final Logger logger = Logger.getLogger(ExtractMidJourneys.class);
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		XMLHandler parser = new XMLHandler(new PlainFactory());
		parser.setValidating(false);
	
		logger.info("Loading persons...");
		parser.parse(args[0]);
		Set<PlainPerson> persons = (Set<PlainPerson>)parser.getPersons();
		logger.info(String.format("Loaded %s persons.", persons.size()));
		
		Set<PlainPerson> newPersons = new HashSet<>();
		for(PlainPerson person : persons) {
			if("midjourneys".equalsIgnoreCase(person.getEpisodes().get(0).getAttribute("datasource"))) {
				newPersons.add(person);
			}
		}
		logger.info(String.format("New population size: %s.", newPersons.size()));
		
		logger.info("Writing persons...");
		XMLWriter writer = new XMLWriter();
		writer.write(args[1], newPersons);
		logger.info("Done.");
	}

}
