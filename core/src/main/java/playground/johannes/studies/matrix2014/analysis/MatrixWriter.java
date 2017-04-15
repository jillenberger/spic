/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2015 by the members listed in the COPYING,       *
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
package playground.johannes.studies.matrix2014.analysis;

import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.data.Person;
import de.dbanalytics.spic.matrix.NumericMatrix;
import de.dbanalytics.spic.matrix.NumericMatrixIO;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author jillenberger
 */
public class MatrixWriter implements AnalyzerTask<Collection<? extends Person>> {

    private final MatrixBuilder matrixBuilder;

    private final FileIOContext ioContext;


    public MatrixWriter(MatrixBuilder builder, FileIOContext ioContext) {
        matrixBuilder = builder;
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Collection<? extends Person> persons, List<StatsContainer> containers) {
        NumericMatrix matrix = matrixBuilder.build(persons);

        try {
            NumericMatrixIO.write(matrix, String.format("%s/matrix.txt.gz", ioContext.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
