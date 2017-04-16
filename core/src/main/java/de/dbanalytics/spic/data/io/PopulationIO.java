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

import java.util.Collection;
import java.util.Set;

/**
 * @author johannes
 */
public class PopulationIO {

    private static final Logger logger = Logger.getLogger(PopulationIO.class);

    public static <P extends Person> Set<P> loadFromXML(String file, Factory factory) {
        XMLHandler parser = new XMLHandler(factory);
        parser.setValidating(false);
        parser.parse(file);

        Set<P> persons = (Set<P>) parser.getPersons();
        logger.info(String.format("Loaded %s persons.", persons.size()));
        return persons;
    }

    public static void writeToXML(String file, Collection<? extends Person> population) {
        XMLWriter writer = new XMLWriter();
        writer.write(file, population);
    }
}
