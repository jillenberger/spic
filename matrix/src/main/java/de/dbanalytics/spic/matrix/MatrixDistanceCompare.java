/*
 * (c) Copyright 2017 Johannes Illenberger
 *
 *  Project de.dbanalytics.spic.*
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dbanalytics.spic.matrix;

import com.vividsolutions.jts.geom.Point;
import de.dbanalytics.spic.analysis.AnalyzerTask;
import de.dbanalytics.spic.analysis.FileIOContext;
import de.dbanalytics.spic.analysis.StatsContainer;
import de.dbanalytics.spic.gis.Feature;
import de.dbanalytics.spic.gis.ZoneIndex;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.hash.TDoubleDoubleHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.matsim.contrib.common.gis.DistanceCalculator;
import org.matsim.contrib.common.gis.OrthodromicDistanceCalculator;
import org.matsim.contrib.common.stats.Discretizer;
import org.matsim.contrib.common.stats.Histogram;
import org.matsim.contrib.common.stats.LinearDiscretizer;
import org.matsim.contrib.common.stats.StatsWriter;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author johannes
 */
public class MatrixDistanceCompare implements AnalyzerTask<Pair<NumericMatrix, NumericMatrix>> {

    private static final Logger logger = Logger.getLogger(MatrixDistanceCompare.class);

    private final String dimension;

    private final NumericMatrix distanceMatrix;

    private final ZoneIndex zoneIndex;

    private DistanceCalculator distanceCalculator;

    private Discretizer discretizer;

    private FileIOContext ioContext;

    public MatrixDistanceCompare(String dimension, ZoneIndex zoneIndex) {
        this.dimension = dimension;
        this.distanceMatrix = new NumericMatrix();
        this.zoneIndex = zoneIndex;

        setDiscretizer(new LinearDiscretizer(50000));
        setDistanceCalculator(OrthodromicDistanceCalculator.getInstance());
    }

    public void setDistanceCalculator(DistanceCalculator calculator) {
        this.distanceCalculator = calculator;
    }

    public void setDiscretizer(Discretizer discretizer) {
        this.discretizer = discretizer;
    }

    public void setFileIoContext(FileIOContext ioContext) {
        this.ioContext = ioContext;
    }

    @Override
    public void analyze(Pair<NumericMatrix, NumericMatrix> matrices, List<StatsContainer> containers) {
        NumericMatrix refMatrix = matrices.getLeft();
        NumericMatrix simMatrix = matrices.getRight();

        TDoubleDoubleHashMap simHist = histogram(simMatrix);
        TDoubleDoubleHashMap refHist = histogram(refMatrix);

        Set<Double> distances = new HashSet();
        for(double d : simHist.keys()) distances.add(d);
        for(double d : refHist.keys()) distances.add(d);

        TDoubleDoubleHashMap diffHist = new TDoubleDoubleHashMap();
        for(Double d : distances) {
            double simVal = simHist.get(d);
            double refVal = refHist.get(d);

            if(refVal == 0 && simVal == 0) {
                diffHist.put(d, 0);
            } else if(refVal > 0) {
                diffHist.put(d, (simVal - refVal)/ refVal);
            }
        }

        containers.add(new StatsContainer(dimension, diffHist.values()));

        if(ioContext != null) {
            try {
                Histogram.normalize(simHist);
                Histogram.normalize(refHist);

                StatsWriter.writeHistogram(simHist, "distance", "count", String.format("%s/%s.sim.txt", ioContext.getPath
                        (), dimension));
                StatsWriter.writeHistogram(refHist, "distance", "count", String.format("%s/%s.ref.txt", ioContext
                        .getPath(), dimension));
                StatsWriter.writeHistogram(diffHist, "distance", "count", String.format("%s/%s.diff.txt", ioContext
                        .getPath(), dimension));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private TDoubleDoubleHashMap histogram(NumericMatrix m) {
        TDoubleArrayList values = new TDoubleArrayList();
        TDoubleArrayList weights = new TDoubleArrayList();

        Set<String> notfound = new HashSet<>();
        Set<String> keys = m.keys();
        for(String i : keys) {
            for(String j : keys) {
                Double vol = m.get(i, j);
                if(vol != null && vol > 0) {
                    double d = getDistance(i, j);
                    if(!Double.isInfinite(d)) {
                        values.add(d);
                        weights.add(vol);
                    } else {
                        if (zoneIndex.get(i) == null) notfound.add(i);
                        if (zoneIndex.get(j) == null) notfound.add(j);
                    }
                }
            }
        }

        if(!notfound.isEmpty()) logger.warn(String.format("Zone %s not found.", notfound.toString()));

        return Histogram.createHistogram(values.toArray(), weights.toArray(), discretizer, true);
    }

    private double getDistance(String i, String j) {
        Double d = distanceMatrix.get(i, j);

        if(d == null) {
            Feature z_i = zoneIndex.get(i);
            Feature z_j = zoneIndex.get(j);

            if(z_i != null && z_j != null) {
                Point p_i = z_i.getGeometry().getCentroid();
                Point p_j = z_j.getGeometry().getCentroid();

                d = distanceCalculator.distance(p_i, p_j);
                distanceMatrix.set(i, j, d);
            } else {
                d = Double.POSITIVE_INFINITY;
            }
        }

        return d;
    }
}
