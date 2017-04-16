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

package de.dbanalytics.spic.data.io.flattable;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;

/**
 * Created by johannesillenberger on 05.04.17.
 */
public class XML2Table {

    private static final Logger logger = Logger.getLogger(XML2Table.class);

    public static void main(String args[]) throws IOException {
        String inFile = args[0];
        String baseFile = args[1];

        logger.info("Loading population...");
        Set<? extends Person> persons = PopulationIO.loadFromXML(inFile, new PlainFactory());
        logger.info("Writing tables...");
        PopulationWriter.write(persons, baseFile);
        logger.info("Done.");
    }
}
