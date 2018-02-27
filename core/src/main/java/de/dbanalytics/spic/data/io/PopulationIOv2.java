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

package de.dbanalytics.spic.data.io;

import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.util.IOUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author jillenberger
 */
public class PopulationIOv2 {

    private static final String POPULATION_ELEMENT = "population";

    private static final String PERSON_ELEMENT = "person";

    private static final String ID_ATTRIBUTE = "id";

    private static final String ATTRIBUTES_ELEMENT = "attributes";

    private static final String ATTRIBUTE_ELEMENT = "attribute";

    private static final String NAME_ATTRIBUTE = "name";

    private static final String EPISODES_ELEMENT = "episodes";

    private static final String EPISODE_ELEMENT = "episode";

    private static final String ACTIVITY_ELEMENT = "activity";

    private static final String LEG_ELEMENT = "leg";

    private static final String NEW_LINE = "\n";

    private static final String SPACES = "    ";

    public static void main(String[] args) throws IOException, XMLStreamException {
        long time = System.currentTimeMillis();
        Set<Person> population = PopulationIO.loadFromXML("/Users/jillenberger/Dropbox/work/hamburg/pop.prepare.xml", new PlainFactory());
        System.out.println("read v1 took " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        PopulationIOv2 populationIOv2 = new PopulationIOv2();
        populationIOv2.write(population, "/Users/jillenberger/Desktop/popv2.xml.gz");
        System.out.println("write v2 took " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        PopulationIO.writeToXML("/Users/jillenberger/Desktop/pop.xml.gz", population);
        System.out.println("write v1 took " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        populationIOv2.read("/Users/jillenberger/Desktop/popv2.xml.gz");
        System.out.println("read v2 took " + (System.currentTimeMillis() - time) + " ms");

        populationIOv2.write(population, "/Users/jillenberger/Desktop/popv2.2.xml.gz");
    }

    public Set<Person> read(String filename) throws IOException, XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader reader = factory.createXMLEventReader(IOUtils.createInputStream(filename));

        Set<Person> population = null;
        Person person = null;
        Episode episode = null;
        Attributable attributable = null;

        QName idQName = new QName(ID_ATTRIBUTE);
        QName nameQName = new QName(NAME_ATTRIBUTE);

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if (localName.equals(POPULATION_ELEMENT)) {
                    /** init poplaiton */
                    population = new LinkedHashSet<>(10000);
                } else if (localName.equals(PERSON_ELEMENT)) {

                    String id = startElement.getAttributeByName(idQName).getValue();
                    person = new PlainPerson(id);
                    attributable = person;
                    population.add(person);

                } else if (localName.equals(ATTRIBUTE_ELEMENT)) {

                    String name = startElement.getAttributeByName(nameQName).getValue();
                    String value = reader.nextEvent().asCharacters().getData();
                    attributable.setAttribute(name, value);

                } else if (localName.equals(EPISODE_ELEMENT)) {

                    episode = new PlainEpisode();
                    attributable = episode;
                    person.addEpisode(episode);

                } else if (localName.equals(ACTIVITY_ELEMENT)) {
                    Segment segment = new PlainSegment();
                    attributable = segment;
                    episode.addActivity(segment);
                } else if (localName.equals(LEG_ELEMENT)) {
                    Segment segment = new PlainSegment();
                    attributable = segment;
                    episode.addLeg(segment);
                }
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (localName.equals(PERSON_ELEMENT)) {
                    person = null;
                    attributable = null;
                } else if (localName.equals(EPISODE_ELEMENT)) {
                    episode = null;
                    attributable = person;
                } else if (localName.equals(ACTIVITY_ELEMENT)) {
                    attributable = episode;
                } else if (localName.equals(LEG_ELEMENT)) {
                    attributable = episode;
                }
            }
        }

        return population;
    }

    public void write(Collection<Person> population, String filename) throws IOException, XMLStreamException {
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        BufferedOutputStream stream = IOUtils.createOutputStream(filename);
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);

        writer.writeStartDocument();
        writer.writeCharacters(NEW_LINE);
        writer.writeStartElement(POPULATION_ELEMENT);
        writer.writeCharacters(NEW_LINE);

        int level = 0;
        for (Person person : population) {
            level++;

            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeStartElement(PERSON_ELEMENT);
            writer.writeAttribute(ID_ATTRIBUTE, person.getId());
            writer.writeCharacters(NEW_LINE);
            level++;

            writeAttributes(person, writer, level, true);

            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeStartElement(EPISODES_ELEMENT);
            writer.writeCharacters(NEW_LINE);

            for (Episode episode : person.getEpisodes()) {
                level++;
                for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                writer.writeStartElement(EPISODE_ELEMENT);
                writer.writeCharacters(NEW_LINE);
                level++;

                writeAttributes(episode, writer, level, true);

                for (int k = 0; k < episode.getActivities().size(); k++) {
                    /** activity */
                    for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                    writer.writeStartElement(ACTIVITY_ELEMENT);
                    writer.writeCharacters(NEW_LINE);
                    writeAttributes(episode.getActivities().get(k), writer, level + 1, false);
                    for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                    writer.writeEndElement();
                    writer.writeCharacters(NEW_LINE);

                    /** leg */
                    if (k < episode.getLegs().size()) {
                        for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                        writer.writeStartElement(LEG_ELEMENT);
                        writer.writeCharacters(NEW_LINE);
                        writeAttributes(episode.getLegs().get(k), writer, level + 1, false);
                        for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                        writer.writeEndElement();
                        writer.writeCharacters(NEW_LINE);
                    }
                }

                level--;
                for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                writer.writeEndElement();
                writer.writeCharacters(NEW_LINE);

                level--;
            }

            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
            level--;

            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
            level--;
        }

        writer.writeEndElement();
        writer.writeCharacters(NEW_LINE);
        writer.writeEndDocument();

        writer.flush();
        writer.close();
        stream.close();
    }

    private void writeAttributes(Attributable attributable, XMLStreamWriter writer, int level, boolean wrap) throws XMLStreamException {
        if (wrap) {
            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeStartElement(ATTRIBUTES_ELEMENT);
            writer.writeCharacters(NEW_LINE);
            level++;
        }

        for (String key : attributable.keys()) {
            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeStartElement(ATTRIBUTE_ELEMENT);
            writer.writeAttribute(NAME_ATTRIBUTE, key);
            writer.writeCharacters(attributable.getAttribute(key));
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
        }

        if (wrap) {
            level--;
            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
        }
    }
}