/*
 * (c) Copyright 2018 Johannes Illenberger
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

package de.dbanalytics.devel.io;

import de.dbanalytics.spic.data.Episode;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.Segment;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.util.IOUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

/**
 * @author jillenberger
 */
public class PopulationIOPbf {

    public static final void main(String[] args) throws IOException {
        String inFileV1 = "/Users/jillenberger/work/spic/core/src/test/resources/populationV1.xml.gz";
        Set<Person> pop = PopulationIO.loadFromXML(inFileV1, new PlainFactory());

        long time = System.currentTimeMillis();
        PopulationIOPbf.write(pop, "/Users/jillenberger/Desktop/popualtion.pbf.gz");
        System.out.println("Time: " + (System.currentTimeMillis() - time));
    }

    public static void write(Collection<? extends Person> population, String file) throws IOException {
        PbfContainers.PbfPopulation.Builder pbfPopulation = PbfContainers.PbfPopulation.newBuilder();
        for (Person person : population) {
            PbfContainers.PbfPerson.Builder pbfPerson = PbfContainers.PbfPerson.newBuilder();
            pbfPerson.setId(person.getId());

            for (String key : person.keys()) {
                pbfPerson.addAttributes(buildAttribute(key, person.getAttribute(key)));
            }

            for (Episode episode : person.getEpisodes()) {
                PbfContainers.PbfEpisode.Builder pbfEpisode = PbfContainers.PbfEpisode.newBuilder();

                for (String key : episode.keys()) {
                    pbfEpisode.addAttributes(buildAttribute(key, episode.getAttribute(key)));
                }

                for (Segment act : episode.getActivities()) {
                    PbfContainers.PbfSegment.Builder pbfSegment = PbfContainers.PbfSegment.newBuilder();
                    for (String key : act.keys()) {
                        pbfSegment.addAttributes(buildAttribute(key, act.getAttribute(key)));
                    }
                    pbfEpisode.addSegments(pbfSegment.build());
                }

                for (Segment leg : episode.getLegs()) {
                    PbfContainers.PbfSegment.Builder pbfSegment = PbfContainers.PbfSegment.newBuilder();
                    for (String key : leg.keys()) {
                        pbfSegment.addAttributes(buildAttribute(key, leg.getAttribute(key)));
                    }
                    pbfEpisode.addSegments(pbfSegment.build());
                }

                pbfPerson.addEpisodes(pbfEpisode.build());
            }

            pbfPopulation.addPopulation(pbfPerson.build());
        }

        PbfContainers.PbfPopulation message = pbfPopulation.build();

        message.writeTo(IOUtils.createOutputStream(file));
    }

    private static PbfContainers.PbfAttribute buildAttribute(String key, String value) {
        PbfContainers.PbfAttribute.Builder pbfAttribute = PbfContainers.PbfAttribute.newBuilder();
        pbfAttribute.setKey(key);
        pbfAttribute.setValue(value);
        return pbfAttribute.build();
    }
}
