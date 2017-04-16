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
import de.dbanalytics.spic.matrix.NumericMatrix;
import gnu.trove.map.TObjectDoubleMap;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class MatrixIntraVolumeShareCompare implements AnalyzerTask<Pair<NumericMatrix, NumericMatrix>> {

    private final FileIOContext ioContext;

    public MatrixIntraVolumeShareCompare(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Pair<NumericMatrix, NumericMatrix> matrices, List<StatsContainer> containers) {
        if (ioContext != null) {
            MatrixIntraVolumeShare task = new MatrixIntraVolumeShare();
            task.analyze(matrices.getLeft(), containers);
            TObjectDoubleMap<String> refRowShares = task.getRowShare();
            TObjectDoubleMap<String> refColShares = task.getColShare();

            task.analyze(matrices.getRight(), containers);
            TObjectDoubleMap<String> simRowShares = task.getRowShare();
            TObjectDoubleMap<String> simColShares = task.getColShare();

            Set<String> keys = matrices.getLeft().keys();
            keys.addAll(matrices.getRight().keys());

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(String.format("%s/intraVolShare.txt", ioContext
                        .getPath())));
                writer.write("id\trefRowShare\tsimRowShare\trefColShare\tsimColShare");
                writer.newLine();
                for (String key : keys) {
                    writer.write(key);
                    writer.write("\t");
                    writer.write(String.valueOf(refRowShares.get(key)));
                    writer.write("\t");
                    writer.write(String.valueOf(simRowShares.get(key)));
                    writer.write("\t");
                    writer.write(String.valueOf(refColShares.get(key)));
                    writer.write("\t");
                    writer.write(String.valueOf(simColShares.get(key)));
                    writer.newLine();
                }

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
