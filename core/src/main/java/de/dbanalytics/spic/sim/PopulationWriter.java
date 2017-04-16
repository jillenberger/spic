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

package de.dbanalytics.spic.sim;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.data.io.PopulationIO;

import java.util.Collection;
import java.util.List;

/**
 * @author johannes
 */
public class PopulationWriter implements AnalyzerTask<Collection<? extends Person>> {

    private final FileIOContext ioContext;

    public PopulationWriter(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> object, List<StatsContainer> containers) {
        if(ioContext != null) {
            PopulationIO.writeToXML(String.format("%s/population.xml.gz", ioContext.getPath()), object);
        }
    }
}
