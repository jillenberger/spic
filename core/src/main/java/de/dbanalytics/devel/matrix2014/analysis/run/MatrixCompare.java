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

package de.dbanalytics.devel.matrix2014.analysis.run;

import de.dbanalytics.devel.matrix2014.analysis.MatrixDistanceCompare;
import de.dbanalytics.devel.matrix2014.analysis.MatrixIntraVolumeShareCompare;
import de.dbanalytics.devel.matrix2014.analysis.MatrixMarginalsCompare;
import de.dbanalytics.devel.matrix2014.analysis.MatrixVolumeCompare;
import de.dbanalytics.spic.analysis.*;
import de.dbanalytics.spic.gis.ZoneCollection;
import de.dbanalytics.spic.gis.ZoneGeoJsonIO;
import de.dbanalytics.spic.matrix.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.gis.CartesianDistanceCalculator;
import org.matsim.contrib.common.stats.LinearDiscretizer;

import java.io.IOException;

/**
 * @author johannes
 */
public class MatrixCompare {

    private static final Logger logger = Logger.getLogger(MatrixCompare.class);

    public static void main(String args[]) throws IOException {
//        String simFile = args[0];
//        String refFile = args[1];
//        String zoneFile = args[2];
//        String outDir = args[3];

        String simFile = "/home/johannes/sge/prj/matrix2014/runs/1141/output/1E9/matrix/matrix.txt.gz";
        String refFile = "/home/johannes/gsv/matrix2014/sim/data/matrices/itp.de.txt";
        String outDir = "/home/johannes/gsv/matrix2014/matrix-compare/";
        String zoneFile = "/home/johannes/gsv/gis/zones/geojson/nuts3.psm.airports.gk3.geojson";
        double volumeThreshold = 0;

//        NumericMatrix simMatrix = GSVMatrixIO.read(simFile);//NumericMatrixIO.read(simFile);
//        NumericMatrix refMatrix = GSVMatrixIO.read(refFile);//NumericMatrixIO.read(refFile);
        NumericMatrix simMatrix = NumericMatrixIO.read(simFile);
        NumericMatrix refMatrix = NumericMatrixIO.read(refFile);

        FileIOContext ioContext = new FileIOContext(outDir);

        ZoneCollection zones = ZoneGeoJsonIO.readFromGeoJSON(zoneFile, "NO", null);
        ODPredicate<String, Double> odPredicate = null;//new ZoneDistancePredicate(zones, 100000,
//                CartesianDistanceCalculator.getInstance());

        NumericMatrix tmpSimMatrix = simMatrix;
        if (odPredicate != null) {
            tmpSimMatrix = (NumericMatrix) MatrixOperations.subMatrix(odPredicate, simMatrix, new NumericMatrix());
        }

        double simTotal = MatrixOperations.sum(tmpSimMatrix);

        NumericMatrix tmpRefMatrix = refMatrix;
        if (odPredicate != null) {
            tmpRefMatrix = (NumericMatrix) MatrixOperations.subMatrix(odPredicate, refMatrix, new NumericMatrix());
        }

        double refTotal = MatrixOperations.sum(tmpRefMatrix);

        if(volumeThreshold > 0) {
            ODPredicate volPredicate = new VolumePredicate(volumeThreshold);
            refMatrix = (NumericMatrix) MatrixOperations.subMatrix(volPredicate, refMatrix, new NumericMatrix());
        }

        logger.debug(String.format("Normalization factor: %s.", simTotal/refTotal));
        MatrixOperations.applyFactor(refMatrix, simTotal / refTotal);

        AnalyzerTaskComposite<Pair<NumericMatrix, NumericMatrix>> composite = new AnalyzerTaskComposite<>();

        HistogramWriter writer = new HistogramWriter(ioContext, new PassThroughDiscretizerBuilder(new
                LinearDiscretizer(0.05), "linear"));

        MatrixVolumeCompare volTask = new MatrixVolumeCompare("matrix.vol");
        volTask.setIoContext(ioContext);
        volTask.setHistogramWriter(writer);

        MatrixDistanceCompare distTask = new MatrixDistanceCompare("matrix.dist", zones);
//        distTask.setDistanceCalculator(WGS84DistanceCalculator.getInstance());
        distTask.setDistanceCalculator(CartesianDistanceCalculator.getInstance());
        distTask.setFileIoContext(ioContext);
        distTask.setDiscretizer(new LinearDiscretizer(25000));

        MatrixMarginalsCompare marTask = new MatrixMarginalsCompare("matrix");
        marTask.setHistogramWriter(writer);

        MatrixIntraVolumeShareCompare intraTask = new MatrixIntraVolumeShareCompare(ioContext);

        composite.addComponent(volTask);
        composite.addComponent(distTask);
        composite.addComponent(marTask);
        composite.addComponent(intraTask);

        AnalyzerTaskRunner.run(new ImmutablePair<>(refMatrix, simMatrix), composite, ioContext);
    }
}
