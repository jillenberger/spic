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

package de.dbanalytics.devel.matrix2014.physics;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.data.io.PopulationIO;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.util.ProgressLogger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * @author johannes
 */
public class AllOrNothing {

    private static final Logger logger = Logger.getLogger(AllOrNothing.class);

    public static void main(String args[]) throws IOException {
        String personsFile = args[0];
        String volumesFile = args[1];

        logger.info("Loading persons...");
        Set<Person> persons = PopulationIO.loadFromXML(personsFile, new PlainFactory());

        logger.info("Calculating link volumes...");
        TObjectDoubleMap<String> volumes = new AllOrNothing().run(persons);

        logger.info("Writing volumes...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(volumesFile));
        writer.write("id\tvolume");
        writer.newLine();

        TObjectDoubleIterator<String> it = volumes.iterator();
        for(int i = 0; i < volumes.size(); i++) {
            it.advance();
            writer.write(it.key());
            writer.write("\t");
            writer.write(String.valueOf(it.value()));
            writer.newLine();
        }
        writer.close();

        logger.info("Done.");
    }

    public TObjectDoubleMap<String> run(Set<Person> persons) {
        TObjectDoubleMap<String> volumes = new TObjectDoubleHashMap<>();

        ProgressLogger.init(persons.size(), 2, 10);

        for (Person p : persons) {
            if(!p.getEpisodes().isEmpty()) {
                double weight = Double.parseDouble(p.getAttribute(CommonKeys.PERSON_WEIGHT));
                Episode e = p.getEpisodes().get(0);

                for (Segment leg : e.getLegs()) {
                    String route = leg.getAttribute(CommonKeys.LEG_ROUTE);

                    if (route != null) {
//                        String linkIds[] = route.split(" ");
//                        for (String id : linkIds) {
//                            volumes.adjustOrPutValue(id, weight, weight);
//                        }
                        String nodeIds[] = route.split("\\s");
                        for (int i = 0; i < nodeIds.length - 1; i++) {
                            String pair = new StringBuilder(32).
                                    append(nodeIds[i]).
                                    append(";").
                                    append(nodeIds[i + 1]).
                                    toString();
                            volumes.adjustOrPutValue(pair, weight, weight);
                        }
                    }
                }
            }

            ProgressLogger.step();
        }

        ProgressLogger.terminate();

        return volumes;
    }
}
