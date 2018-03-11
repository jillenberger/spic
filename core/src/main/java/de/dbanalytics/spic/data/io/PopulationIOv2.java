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

import com.fasterxml.aalto.stax.InputFactoryImpl;
import de.dbanalytics.spic.data.*;
import de.dbanalytics.spic.util.IOUtils;
import de.dbanalytics.spic.util.ProgressLogger;
import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLOutputFactory2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
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

    private static final Logger logger = Logger.getLogger(PopulationIOv2.class);

    private static final String POPULATION_ELEMENT = "population";

    private static final String PERSON_ELEMENT = "person";

    private static final String ID_ATTRIBUTE = "id";

    private static final String ATTRIBUTES_ELEMENT = "attributes";

    private static final String ATTRIBUTE_ELEMENT = "attribute";

    private static final String NAME_ATTRIBUTE = "name";

    private static final String EPISODE_ELEMENT = "episode";

    private static final String ACTIVITY_ELEMENT = "activity";

    private static final String LEG_ELEMENT = "leg";

    private static final String SIZE = "size";

    private static final String NEW_LINE = "\n";

    private static final String SPACES = "    ";

    public static void main(String[] args) throws IOException, XMLStreamException, InterruptedException {
//        Thread.sleep(10000);
        long time = System.currentTimeMillis();
        Set<Person> population = PopulationIO.loadFromXML("/Users/jillenberger/Dropbox/work/hamburg/pop.prepare.xml", new PlainFactory());
        System.out.println("read v1 took " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        PopulationIOv2 populationIOv2 = new PopulationIOv2();
        populationIOv2.write(population, "/Users/jillenberger/Desktop/popv2.xml");
        System.out.println("write v2 took " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        PopulationIO.writeToXML("/Users/jillenberger/Desktop/pop.xml.gz", population);
        System.out.println("write v1 took " + (System.currentTimeMillis() - time) + " ms");

        time = System.currentTimeMillis();
        populationIOv2.read("/Users/jillenberger/Desktop/popv2.xml");
        System.out.println("read v2 took " + (System.currentTimeMillis() - time) + " ms");

        populationIOv2.write(population, "/Users/jillenberger/Desktop/popv2.2.xml");
    }

    public static Set<Person> read(String filename) throws IOException, XMLStreamException {
        long time = System.currentTimeMillis();

        InputFactoryImpl factory = new InputFactoryImpl();
        factory.configureForSpeed();
        XMLEventReader reader = factory.createXMLEventReader(IOUtils.createInputStream(filename));

        Set<Person> population = null;
        Person person = null;
        Episode episode = null;
        Attributable attributable = null;

        QName idQName = new QName(ID_ATTRIBUTE);
        QName nameQName = new QName(NAME_ATTRIBUTE);

        ProgressLogger progressLogger = null;

        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();

            if (event.isStartElement()) {
                StartElement startElement = event.asStartElement();
                String localName = startElement.getName().getLocalPart();

                if (localName.equals(POPULATION_ELEMENT)) {
                    /** init poplation */
                    Attribute attribute = startElement.getAttributeByName(new QName(SIZE));
                    if (attribute != null) {
                        int size = Integer.parseInt(attribute.getValue());
                        population = new LinkedHashSet<>(size);
                        progressLogger = new ProgressLogger(logger);
                        progressLogger.start("Loading population...", size);
                    } else {
                        population = new LinkedHashSet<>();
                    }

                } else if (localName.equals(PERSON_ELEMENT)) {
                    /** create person */
                    String id = startElement.getAttributeByName(idQName).getValue();
                    person = new PlainPerson(id);
                    population.add(person);
                    attributable = person;

                } else if (localName.equals(ATTRIBUTE_ELEMENT)) {
                    /** add attributes to the current element */
                    String name = startElement.getAttributeByName(nameQName).getValue();
                    String value = reader.nextEvent().asCharacters().getData();
                    attributable.setAttribute(name, value);

                } else if (localName.equals(EPISODE_ELEMENT)) {
                    /** create episode */
                    episode = new PlainEpisode();
                    person.addEpisode(episode);
                    attributable = episode;

                } else if (localName.equals(ACTIVITY_ELEMENT)) {
                    /** create segment */
                    Segment segment = new PlainSegment();
                    episode.addActivity(segment);
                    attributable = segment;

                } else if (localName.equals(LEG_ELEMENT)) {
                    /** create segment */
                    Segment segment = new PlainSegment();
                    episode.addLeg(segment);
                    attributable = segment;

                }
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String localName = endElement.getName().getLocalPart();

                if (localName.equals(PERSON_ELEMENT)) {
                    person = null;
                    attributable = null;
                    if (progressLogger != null) progressLogger.step();
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

        if (progressLogger != null) {
            progressLogger.stop(String.format(
                    "%s people in %.2f secs.",
                    population.size(),
                    (System.currentTimeMillis() - time) / 1000.0));
        }

        return population;
    }

    public static void write(Collection<Person> population, String filename) throws IOException, XMLStreamException {
        ProgressLogger progressLogger = new ProgressLogger(logger);
        progressLogger.start("Writing population...", population.size());
        long time = System.currentTimeMillis();

        XMLOutputFactory outputFactory = XMLOutputFactory2.newInstance();
        BufferedOutputStream stream = IOUtils.createOutputStream(filename);
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);

        writer.writeStartDocument();
        writer.writeCharacters(NEW_LINE);
        writer.writeStartElement(POPULATION_ELEMENT);
        writer.writeAttribute(SIZE, String.valueOf(population.size()));
        writer.writeCharacters(NEW_LINE);

        int level = 0;
        for (Person person : population) {
            level++;

            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeStartElement(PERSON_ELEMENT);
            writer.writeAttribute(ID_ATTRIBUTE, person.getId());
            writer.writeCharacters(NEW_LINE);
            level++;

            writeAttributes(person, writer, level, false);

            for (Episode episode : person.getEpisodes()) {
                for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
                writer.writeStartElement(EPISODE_ELEMENT);
                writer.writeCharacters(NEW_LINE);
                level++;

                writeAttributes(episode, writer, level, false);

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
            }

            level--;

            for (int i = 0; i < level; i++) writer.writeCharacters(SPACES);
            writer.writeEndElement();
            writer.writeCharacters(NEW_LINE);
            level--;

            progressLogger.step();
        }

        writer.writeEndElement();
        writer.writeCharacters(NEW_LINE);
        writer.writeEndDocument();

        writer.flush();
        writer.close();
        stream.close();

        progressLogger.stop(String.format(
                "%s people in %.2f secs.",
                population.size(),
                (System.currentTimeMillis() - time) / 1000.0));
    }

    private static void writeAttributes(Attributable attributable, XMLStreamWriter writer, int level, boolean wrap) throws XMLStreamException {
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
