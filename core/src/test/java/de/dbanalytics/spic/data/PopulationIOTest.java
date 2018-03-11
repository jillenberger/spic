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

package de.dbanalytics.spic.data;

import de.dbanalytics.spic.data.io.PopulationIO;
import de.dbanalytics.spic.util.TestCaseUtils;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author jillenberger
 */
public class PopulationIOTest extends TestCase {

    public void testReadWriteXML() throws IOException {
        String tmpDir = "src/test/tmp/";

        new File(tmpDir).mkdirs();
        TestCaseUtils.deleteDir(tmpDir);

        String inFileV1 = "src/test/resources/populationV1.xml.gz";

        String outFile1V2 = tmpDir + "pop1.xml";
        String outFile2V2 = tmpDir + "pop2.xml";
        String outFileV1 = tmpDir + "pop3.xml";

        Set<Person> pop = PopulationIO.loadFromXML(inFileV1, new PlainFactory());
        PopulationIO.writeToXML(outFile1V2, pop);
        PopulationIO.loadFromXML(outFile1V2, new PlainFactory());
        PopulationIO.writeToXML(outFile2V2, pop);

        TestCaseUtils.compareFiles(outFile1V2, outFile2V2);

        PopulationIO.writeToXMLV1(outFileV1, pop);
        TestCaseUtils.compareFiles(inFileV1, outFileV1);
    }
}
