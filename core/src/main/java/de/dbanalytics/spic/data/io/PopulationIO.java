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

package de.dbanalytics.spic.data.io;

import de.dbanalytics.spic.data.Factory;
import de.dbanalytics.spic.data.Person;
import org.apache.log4j.Logger;

import javax.xml.stream.XMLStreamException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author johannes
 */
public class PopulationIO {

    private static final Logger logger = Logger.getLogger(PopulationIO.class);

    public static <P extends Person> Set<P> loadFromXML(String file, Factory factory) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            line = reader.readLine();
            reader.close();
            if (line.matches("<population size=\"[0-9]+\">")) {
                return readV2(file);
            } else {
                return readV1(file, factory);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static <P extends Person> Set<P> readV1(String file, Factory factory) {
        XMLHandler parser = new XMLHandler(factory);
        parser.setValidating(false);
        parser.parse(file);

        Set<P> persons = (Set<P>) parser.getPersons();
        logger.info(String.format("Loaded %s persons.", persons.size()));
        return persons;
    }

    private static <P extends Person> Set<P> readV2(String file) {
        try {
            return (Set<P>) PopulationIOv2.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void writeToXML(String file, Collection<? extends Person> population) {
        /** sort persons for better file comparison in test cases */
        SortedSet<Person> sorted = new TreeSet<>(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        sorted.addAll(population);

        try {
            PopulationIOv2.write(sorted, file);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public static void writeToXMLV1(String file, Collection<? extends Person> population) {
        XMLWriter writer = new XMLWriter();

        /** sort persons for better file comparison in test cases */
        SortedSet<Person> sorted = new TreeSet<>(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getId().compareTo(o2.getId());
            }
        });
        sorted.addAll(population);

        writer.write(file, sorted);
    }
}
