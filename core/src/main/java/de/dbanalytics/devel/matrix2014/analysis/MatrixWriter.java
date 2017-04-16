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
package de.dbanalytics.devel.matrix2014.analysis;

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
