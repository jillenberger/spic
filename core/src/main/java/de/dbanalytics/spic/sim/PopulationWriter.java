/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

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
