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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.source.mid2008HH;

import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.PlainFactory;
import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.processing.PersonTask;
import de.dbanalytics.spic.processing.TaskRunner;
import de.dbanalytics.spic.source.mid2008.MiDKeys;

import java.util.Set;

/**
 * Created by johannesillenberger on 04.05.17.
 */
public class RunFixLau2Class {

    public static void main(String args[]) {
        String inFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/demand/midHH/mid2008HH.xml";
        String outFile = "/home/johannesillenberger/gsv/C_Vertrieb/2017_03_21_DRIVE/97_Work/demand/midHH/mid2008HH2.xml";
        Set<Person> persons = PopulationIO.loadFromXML(inFile, new PlainFactory());
        TaskRunner.run(new PersonTask() {
            @Override
            public void apply(Person person) {
                String value = person.getAttribute(MiDKeys.PERSON_LAU2_CLASS);
                if (value != null) {
                    int klass = Integer.parseInt(value);
                    klass = Math.max(0, klass - 1);

                    person.setAttribute(MiDKeys.PERSON_LAU2_CLASS, String.valueOf(klass));

                }
            }
        }, persons);
        PopulationIO.writeToXML(outFile, persons);
    }
}
